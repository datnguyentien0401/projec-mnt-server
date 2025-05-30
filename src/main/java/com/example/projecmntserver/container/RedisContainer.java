package com.example.projecmntserver.container;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
@TestConfiguration
public class RedisContainer {
    private static final Set<Integer> redisClusterPorts = Set.of(7000, 7001, 7002, 7003, 7004, 7005);
    private static final String REDIS_IMAGE_NAME = "grokzen/redis-cluster:7.0.7";
    private static final GenericContainer<?> redisClusterContainer = new GenericContainer<>(REDIS_IMAGE_NAME)
            .withExposedPorts(redisClusterPorts.toArray(new Integer[0]))
            .withCreateContainerCmdModifier(cmd -> {
                cmd.getHostConfig()
                   .withMemory(TestContainerConstant.DEFAULT_MEMORY_IN_BYTES)
                   .withMemorySwap(TestContainerConstant.DEFAULT_MEMORY_SWAP_IN_BYTES)
                   .withCpuCount(TestContainerConstant.DEFAULT_CPU_COUNT)
                   .withCpuPercent(TestContainerConstant.DEFAULT_CPU_PERCENT);
            });
    private static final ConcurrentMap<Integer, Integer> redisClusterNatPortMapping = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, SocketAddress> redisClusterSocketAddresses = new ConcurrentHashMap<>();

    static {
        redisClusterContainer.start();

        final String redisClusterNodes = redisClusterPorts.stream()
                                                          .map(port -> {
                                                              final Integer mappedPort = redisClusterContainer.getMappedPort(port);
                                                              redisClusterNatPortMapping.put(port, mappedPort);
                                                              return redisClusterContainer.getHost() + ':' + mappedPort;
                                                          })
                                                          .collect(Collectors.joining(","));

        System.setProperty("spring.data.redis.cluster.nodes", redisClusterNodes);
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        final SocketAddressResolver socketAddressResolver = new SocketAddressResolver() {
            @Override
            public SocketAddress resolve(RedisURI redisURI) {
                final Integer mappedPort = redisClusterNatPortMapping.get(redisURI.getPort());
                if (mappedPort != null) {
                    final SocketAddress socketAddress = redisClusterSocketAddresses.get(mappedPort);
                    if (socketAddress != null) {
                        return socketAddress;
                    }
                    redisURI.setPort(mappedPort);
                }

                redisURI.setHost(DockerClientFactory.instance().dockerHostIpAddress());

                final SocketAddress socketAddress = super.resolve(redisURI);
                redisClusterSocketAddresses.putIfAbsent(redisURI.getPort(), socketAddress);
                return socketAddress;
            }
        };
        return ClientResources.builder()
                              .socketAddressResolver(socketAddressResolver)
                              .build();
    }
}

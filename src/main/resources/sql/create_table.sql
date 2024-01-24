create table plannings
(
    id                      bigint auto_increment
        primary key,
    table_key               varchar(64)  not null,
    name                    varchar(256) null,
    available_working_data  json         not null,
    required_workforce_data json         null,
    total_workforce_data    json         null,
    annual_leave_data       json         null,
    from_date               datetime     null,
    to_date                 datetime     null,
    created_at              datetime     null,
    updated_at              datetime     null
);

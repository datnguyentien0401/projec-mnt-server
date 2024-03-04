package com.example.projecmntserver.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.dto.mapper.TeamMapper;
import com.example.projecmntserver.dto.request.TeamDto;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.dto.response.TeamResponse;
import com.example.projecmntserver.dto.response.TeamViewResponse;
import com.example.projecmntserver.service.TeamService;
import com.example.projecmntserver.util.DatetimeUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teams")
@Validated
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final TeamMapper teamMapper;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(teamMapper.toResponse(teamService.findAll()));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamDto teamDto) {
        return ResponseEntity.ok(teamMapper.toResponse(teamService.create(teamDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> create(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overall")
    public ResponseEntity<List<OverallTeamResponse>> getOverall(@RequestParam String fromDate,
                                                                @RequestParam String toDate) {
        return ResponseEntity.ok(teamService.getOverall(DatetimeUtils.parse(fromDate),
                                                        DatetimeUtils.parse(toDate)));
    }

    @GetMapping("/{id}/team-view")
    public ResponseEntity<TeamViewResponse> getTeamView(@PathVariable Long id,
                                                        @RequestParam String fromDate,
                                                        @RequestParam String toDate) {
        return ResponseEntity.ok(teamService.getTeamView(DatetimeUtils.parse(fromDate),
                                                         DatetimeUtils.parse(toDate),
                                                         id));
    }
}

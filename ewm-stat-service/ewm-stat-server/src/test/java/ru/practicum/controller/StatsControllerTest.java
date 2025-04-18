package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.stats.controller.StatsController;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void hit_shouldReturn201() throws Exception {
        String jsonHit = "{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"ip\":\"192.163.0.1\",\"timestamp\":\"2023-07-19 10:00:00\"}";

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonHit))
                .andExpect(status().isCreated());

        verify(statsService).saveHit(any(EndpointHit.class));
    }


    @Test
    void getStats_shouldReturn200() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(List.of(new ViewStats("ewm-main-service", "/events/1", 10L)));

        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", "/events/1")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(10));
    }
}
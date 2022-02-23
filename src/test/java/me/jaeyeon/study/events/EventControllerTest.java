package me.jaeyeon.study.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("정상적으로 이벤트를 생성하는 이벤트")
    void createEVent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2021, 11, 21, 12, 00))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 22, 12, 00))
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 12, 00))
                .endEventDateTime(LocalDateTime.of(2021, 11, 24, 12, 00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON) // 요청 타입
                        .accept(MediaTypes.HAL_JSON) // 받고 싶은 타입
                        .content(objectMapper.writeValueAsString(event))) // event를 json을 String으로 맵핑
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists()) // 201 상태인지 확인
                .andExpect(header().exists(HttpHeaders.LOCATION)) // Header에 Location 있는지 확인
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false)) // free가 false
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))

                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists());
    }

    @Test
    @DisplayName("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    void createEVent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2021, 11, 21, 12, 00))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 22, 12, 00))
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 12, 00))
                .endEventDateTime(LocalDateTime.of(2021, 11, 24, 12, 00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    void createEvent_Bad_Request_wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEventDateTime(LocalDateTime.of(2021, 11, 21, 12, 00))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 20, 12, 00))
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 19, 12, 00))
                .endEventDateTime(LocalDateTime.of(2021, 11, 24, 12, 00))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }
}
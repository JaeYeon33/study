package me.jaeyeon.study.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test // EventDTO를 사용하여 값을 만든경우 잘동작함
    @DisplayName("정상적으로 이벤트를 생성하는 테스트")
    void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 02, 24, 21, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 25, 21, 22))
                .beginEventDateTime(LocalDateTime.of(2022, 02, 26, 21, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 27, 22, 23))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON) // 요청타입
                    .accept(MediaTypes.HAL_JSON)             // 받고 싶은 타입
                    .content(objectMapper.writeValueAsString(event)))                             // Event를 json -> string 맵핑
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())                                     // 201 상태인지 확인
                .andExpect(header().exists(HttpHeaders.LOCATION))                                 // ID가 있는지 확인
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))  // Content-Type 값 확인
                .andExpect(jsonPath("id").value(Matchers.not(100)))               // ID가 100이 아니면
                .andExpect(jsonPath("free").value(false))                  // free가 true라면
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()));
    }

    @Test // Event를 사용해서 이상한 값들을 넣어주면 응답이 isBadRequest로 나오길 바람
    @DisplayName("입력 받을 수 없는 값을 사용하는 경우에 에러가 발생하는 테스트")
    void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100L)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 02, 24, 21, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 24, 22, 22))
                .beginEventDateTime(LocalDateTime.of(2022, 02, 24, 23, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 24, 23, 23))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON) // 요청타입
                        .accept(MediaTypes.HAL_JSON)             // 받고 싶은 타입
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }

    @Test
    @DisplayName("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 02, 26, 21, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 25, 22, 22))
                .beginEventDateTime(LocalDateTime.of(2022, 02, 24, 23, 22))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 02, 23, 23, 23))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }
}
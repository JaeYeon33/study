package me.jaeyeon.study.events;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {

        // TODO 일반적인 Bad_REquest 처리
        // TODO build() -> 에러 메시지 body(errors)에 담기
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // TODO 특정한 값 Bad_Request 처리
        // TODO build() -> 에러 메시지 body(errors)에 담기
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // TODO Model Mapper 사용 후 입력값 제한하기
        Event event = modelMapper.map(eventDto, Event.class);

        // TODO 저장하기 전에 유료인지 무료인지 여부 업데이트 (비즈니스 로직 적용)
        event.update();

        // TODO DB에 ID가 있는지 확인
        Event newEvent = eventRepository.save(event);

        // TODO HATEOAS 적용-1
        // TODO Locadtion 헤더 정보에 URI 담기
        // TODO ControllerLinkBuilder(2.1.0.RELEASE) -> WebMvcLinkBuilder(2.2.5.RELEASE)
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class)
                .slash(newEvent.getId());

        URI createUri = selfLinkBuilder.toUri();

        // TODO HATEOAS 적용-2
        EventResource eventResource = new EventResource(newEvent);
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        // TODO _links.self(EventResource로 옮김)
//        eventResource.add(selfLinkBuilder.withRel());

        return ResponseEntity.created(createUri).body(eventResource);
    }
}
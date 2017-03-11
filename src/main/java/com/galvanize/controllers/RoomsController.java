package com.galvanize.controllers;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.galvanize.models.Room;
import com.galvanize.repositories.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RoomsController {

    @Autowired
    private RoomsRepository roomsRepository;

    @RequestMapping(value = "/rooms", method = POST)
    @ResponseStatus(CREATED)
    public Room createRoom(@Valid @RequestBody Room room) {
        Room newRoom = new Room();
        newRoom.setName(room.getName());
        newRoom.setCapacity(room.getCapacity());
        newRoom.setHavingVc(room.isHavingVc());
        roomsRepository.save(newRoom);
        return newRoom;
    }

    @ExceptionHandler
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleException(MethodArgumentNotValidException e) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("reason", UNPROCESSABLE_ENTITY.getReasonPhrase());
        errorBody.put("errors", e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(toList()));
        return errorBody;
    }
}
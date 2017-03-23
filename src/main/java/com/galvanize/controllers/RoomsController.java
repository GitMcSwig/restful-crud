package com.galvanize.controllers;

import static java.util.stream.Collectors.toList;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.galvanize.models.Room;
import com.galvanize.repositories.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@ResponseStatus(NOT_FOUND)
class RoomNotFoundException extends RuntimeException {
}

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

    @RequestMapping(value = "/rooms", method = GET)
    public List<Room> getRooms() {
        return roomsRepository.findAll();
    }

    @RequestMapping(value="/rooms/{id}", method = GET)
    public Room getRoom(@PathVariable("id") String id) {
        Room found = roomsRepository.findOne(id);
        if (found != null) {
            return found;
        }
        else {
            throw new RoomNotFoundException();
        }
    }

    @RequestMapping(value = "/rooms/{id}", method =  PUT)
    @ResponseStatus(NO_CONTENT)
    public void putRoom(@PathVariable("id") String id, @Valid @RequestBody Room room) {
        Room newRoom = new Room();
        newRoom.setId(room.getId());
        newRoom.setName(room.getName());
        newRoom.setCapacity(room.getCapacity());
        newRoom.setHavingVc(room.isHavingVc());
        roomsRepository.save(newRoom);
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
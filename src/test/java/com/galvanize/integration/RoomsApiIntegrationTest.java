package com.galvanize.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galvanize.Application;
import com.galvanize.models.Room;
import com.galvanize.repositories.RoomsRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomsApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RoomsRepository roomsRepository;

    private String BASE_URL;

    @Before
    public void setup() {
        BASE_URL = "http://localhost:"+port+"/rooms/";
        roomsRepository.deleteAll();
    }

    @Test
    public void postRespondsWithStatusCodeCreated() {

        Room room = new Room();
        room.setName("Ruby");
        room.setCapacity(12);
        room.setHavingVc(true);

        ResponseEntity<Room> response = restTemplate.postForEntity(
                BASE_URL, room, Room.class);
        assertThat(response.getStatusCode(), equalTo(CREATED));
    }

    @Test
    public void postRespondsWithCreatedRoom() {

        Room room = new Room();
        room.setName("Ruby");
        room.setCapacity(12);
        room.setHavingVc(true);

        ResponseEntity<Room> response = restTemplate.postForEntity(
                BASE_URL, room, Room.class);

        Room newRoom = response.getBody();
        assertThat(newRoom, notNullValue());
        assertThat(newRoom.getId(), notNullValue());
        assertThat(newRoom.getName(), equalTo("Ruby"));
        assertThat(newRoom.getCapacity(), equalTo(12));
        assertThat(newRoom.isHavingVc(), equalTo(true));
    }

    @Test
    public void addsTheInstanceToTheDatabase() {

        Room room = new Room();
        room.setName("Ruby");
        room.setCapacity(12);
        room.setHavingVc(false);

        restTemplate.postForEntity(BASE_URL, room, Room.class);

        assertThat(roomsRepository.count(), equalTo(1L));
    }

    @Test
    public void postRespondsWithStatusCodeUnprocessableEntityForRoomWithEmptyName() {

        Room room = new Room();
        room.setName("");
        room.setCapacity(12);
        room.setHavingVc(true);

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL, room, String.class);

        assertThat(response.getStatusCode(), equalTo(UNPROCESSABLE_ENTITY));
    }

    @Test
    public void postRespondsWithDetailsOfValidationError() throws Exception {

        Room room = new Room();
        room.setName("");
        room.setCapacity(12);
        room.setHavingVc(true);

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL, room, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> error = objectMapper.readValue(response.getBody(),
                new TypeReference<Map<String, Object>>() {});

        assertThat(error.get("reason"), equalTo("Unprocessable Entity"));
        assertThat(error.get("errors"), equalTo(singletonList("Name must not be empty!")));
    }

    @Test
    public void getRoomsRespondsWithAListOfRoomsWithStatus200OK() throws Exception {
        Room room1 = new Room();
        room1.setName("Ruby");
        room1.setCapacity(12);
        room1.setHavingVc(true);

        Room room2 = new Room();
        room2.setName("Ruby");
        room2.setCapacity(12);
        room2.setHavingVc(true);

        roomsRepository.deleteAll();
        roomsRepository.save(room1);
        roomsRepository.save(room2);

        ResponseEntity<List> response = restTemplate.getForEntity(BASE_URL, List.class);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().size(), equalTo(2));
    }

    @Test
    public void getRoomsRespondsWithAnEmptyListOfRoomsWithStatus200OK() throws Exception {
        roomsRepository.deleteAll();

        ResponseEntity<List> response = restTemplate.getForEntity(BASE_URL, List.class);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().size(), equalTo(0));
    }

    @Test
    public void getRoomRespondsWithRoomAndStatus200OKIfRoomExists() throws Exception {
        Room room1 = new Room();
        room1.setName("Test");
        room1.setCapacity(12);
        room1.setHavingVc(true);

        Room savedRoom = roomsRepository.save(room1);


        ResponseEntity<Room> response = restTemplate.getForEntity(BASE_URL + savedRoom.getId(), Room.class, String.class);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getId(), equalTo(savedRoom.getId()));
        assertThat(response.getBody().getName(), equalTo(room1.getName()));
    }

    @Test
    public void getRoomRespondsWithRoomAndStatus404NotFoundIfRoomDoesNotExist() throws Exception {
        roomsRepository.deleteAll();


        ResponseEntity<Room> response = restTemplate.getForEntity(BASE_URL+ "XCVGG", Room.class, String.class);
        assertThat(response.getStatusCode(), equalTo(NOT_FOUND));
    }

    @Test
    public void putRoomsUpdatesTheRoomAndRespondsWith204IfEntityHasBeenUpdated() throws Exception {
        roomsRepository.deleteAll();

        Room room1 = new Room();
        room1.setName("Update");
        room1.setCapacity(12);
        room1.setHavingVc(false);

        Room saved = roomsRepository.save(room1);
        saved.setHavingVc(true);

        URI uri = new URI(BASE_URL + saved.getId());
        HttpEntity<Room> roomHttpEntity = new HttpEntity<Room>(saved);

        ResponseEntity<Room> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, roomHttpEntity, Room.class);

        assertThat(responseEntity.getStatusCode(), equalTo(NO_CONTENT));
        assertThat(responseEntity.getBody(), equalTo(null));

        Room updated = roomsRepository.findOne(saved.getId());
        assertThat(updated.isHavingVc(), equalTo(true));
    }

    @Test
    public void putRoomsDoesNotUpdateAnInvalidRoomAndRespondsWith422() throws Exception {
        roomsRepository.deleteAll();

        Room room1 = new Room();
        room1.setId("CRAP");
        room1.setName("");
        room1.setCapacity(12);
        room1.setHavingVc(false);


        URI uri = new URI(BASE_URL + "CRAP");
        HttpEntity<Room> roomHttpEntity = new HttpEntity<Room>(room1);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, roomHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), equalTo(UNPROCESSABLE_ENTITY));

        List<Room> rooms = roomsRepository.findAll();
        assertThat(rooms.size(), equalTo(0));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> error = objectMapper.readValue(responseEntity.getBody(),
                new TypeReference<Map<String, Object>>() {});

        assertThat(error.get("reason"), equalTo("Unprocessable Entity"));
        assertThat(error.get("errors"), equalTo(singletonList("Name must not be empty!")));

    }

}
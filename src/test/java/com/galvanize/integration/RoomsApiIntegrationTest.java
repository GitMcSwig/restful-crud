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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomsApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RoomsRepository roomsRepository;


    @Before
    public void setup() {
        roomsRepository.deleteAll();
    }

    @Test
    public void postRespondsWithStatusCodeCreated() {
        String BASE_URL = "http://localhost:"+port+"/rooms/";

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
        String BASE_URL = "http://localhost:"+port+"/rooms/";

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
        String BASE_URL = "http://localhost:"+port+"/rooms/";

        Room room = new Room();
        room.setName("Ruby");
        room.setCapacity(12);
        room.setHavingVc(false);

        restTemplate.postForEntity(BASE_URL, room, Room.class);

        assertThat(roomsRepository.count(), equalTo(1L));
    }

    @Test
    public void postRespondsWithStatusCodeUnprocessableEntityForRoomWithEmptyName() {
        String BASE_URL = "http://localhost:"+port+"/rooms/";

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
        String BASE_URL = "http://localhost:"+port+"/rooms/";

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
}
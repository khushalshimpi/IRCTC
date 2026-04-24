package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.User;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class UserBookingService {

    private User user;

    private List<User> userList;

//    we use object mapper to extract the json data so here we use jackson objectMapper
//    serialize - obj to json
//    desrialize - json to obj conversion means user_id Map -> userId of entities

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "app/src/main/java/org/example/localDb/users.json";

//    typeReference we use to handle our object data in runtine so it can't cause type issue while json->obj data access
    public void UserBookingService(User user1) throws IOException {
        this.user = user1;
        File users = new File(USERS_PATH);
        userList = objectMapper.readValue(users, new TypeReference<List<User>>() {});
    }
}

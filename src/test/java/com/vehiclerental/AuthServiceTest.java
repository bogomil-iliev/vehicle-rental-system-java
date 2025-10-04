package com.vehiclerental;

import com.vehiclerental.dao.UserDAO;
import com.vehiclerental.models.User;
import com.vehiclerental.services.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/* Unit tests 2 & 3 (UT-2 and UT-3 ) for login/registration logic in AuthService. */
class AuthServiceTest {

    // login() should return null when the password is wrong.
     
    @Test
    void login_wrongPassword_returnsNull() throws Exception {

        //Mock any new UserDAO() call inside AuthService
        try (MockedConstruction<UserDAO> mocked =
                     Mockito.mockConstruction(UserDAO.class, (mock, ctx) -> {
                         // getAllUsers() is first method executed
                         Mockito.when(mock.getAllUsers()).thenReturn(Collections.emptyList());
                         // register() will call saveUser(...)
                         Mockito.when(mock.saveUser(Mockito.any(User.class))).thenReturn(true);
                     })) {

            AuthService auth = new AuthService();// uses mocked DAO
            auth.register("alice", "correctPwd", "CUSTOMER");// adds in-memory user

            //Act & Assert 
            assertNull(auth.login("alice", "wrongPwd")); // wrong password
            assertNotNull(auth.login("alice", "correctPwd")); // control check
        }
    }

    //register() should fail when username already exists.
     
    @Test
    void register_duplicateUser_returnsFalse() throws Exception {

        try (MockedConstruction<UserDAO> mocked =
                     Mockito.mockConstruction(UserDAO.class, (mock, ctx) -> {
                         Mockito.when(mock.getAllUsers()).thenReturn(Collections.emptyList());
                         Mockito.when(mock.saveUser(Mockito.any(User.class))).thenReturn(true);
                     })) {

            AuthService auth = new AuthService();
            assertTrue(auth.register("bob", "pw", "ADMIN"));  // first time succeeds
            assertFalse(auth.register("bob", "pw", "ADMIN")); // duplicate rejected
        }
    }
}

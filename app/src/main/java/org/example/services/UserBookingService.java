package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.example.entities.Train;
import org.example.entities.Ticket;
import org.example.entities.User;
import org.example.util.LocalDb;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private List<User> userList;

    private User user;

    private final File usersFile;

    public UserBookingService(User user) throws IOException {
        this.user = user;
        this.usersFile = LocalDb.usersFile().toFile();
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        this.usersFile = LocalDb.usersFile().toFile();
        loadUserListFromFile();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            boolean exists = userList.stream().anyMatch(u -> u.getName().equalsIgnoreCase(user1.getName()));
            if (exists) {
                return Boolean.FALSE;
            }
            userList.add(user1);
            saveUserListToFile();
            this.user = user1;
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBookings(){
        if (user == null) {
            System.out.println("Please login first to fetch bookings.");
            return;
        }
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        } else {
            System.out.println("No bookings found (or not logged in).");
        }
    }

    public Boolean cancelBooking(String ticketId){
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        Optional<User> userFetched = findLoggedInUser();
        if (userFetched.isEmpty()) {
            System.out.println("Please login first to cancel bookings.");
            return Boolean.FALSE;
        }

        User storedUser = userFetched.get();
        if (storedUser.getTicketsBooked() == null) {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }

        boolean removed = storedUser.getTicketsBooked().removeIf(ticket -> ticketId.equals(ticket.getTicketId()));
        if (!removed) {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }

        try {
            saveUserListToFile();
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } catch (IOException e) {
            System.out.println("Failed to save cancellation.");
            return Boolean.FALSE;
        }
    }

    public Boolean bookTicket(Train train, int row, int seat, String source, String destination, String dateOfTravel) {
        Optional<User> userFetched = findLoggedInUser();
        if (userFetched.isEmpty()) {
            System.out.println("Please login first to book a seat.");
            return Boolean.FALSE;
        }

        Boolean seatBooked = bookTrainSeat(train, row, seat);
        if (!Boolean.TRUE.equals(seatBooked)) {
            return Boolean.FALSE;
        }

        User storedUser = userFetched.get();
        if (storedUser.getTicketsBooked() == null) {
            storedUser.setTicketsBooked(new ArrayList<>());
        }

        String travelDate = (dateOfTravel == null || dateOfTravel.isBlank()) ? new Date().toString() : dateOfTravel;
        Ticket ticket = new Ticket(UUID.randomUUID().toString(), storedUser.getUserId(), source, destination, travelDate, train);
        storedUser.getTicketsBooked().add(ticket);

        try {
            saveUserListToFile();
            System.out.println("Ticket booked with ID: " + ticket.getTicketId());
            return Boolean.TRUE;
        } catch (IOException e) {
            System.out.println("Seat booked, but failed to save ticket to user.");
            return Boolean.FALSE;
        }
    }

    private Optional<User> findLoggedInUser() {
        if (user == null) {
            return Optional.empty();
        }
        return userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();
    }


    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }
}

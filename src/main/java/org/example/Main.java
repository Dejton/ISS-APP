package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Scanner;

public class Main {

    private static final String ISS_API_LOCATION = "http://api.open-notify.org/iss-now.json";
    private static final String ISS_API_PEOPLE = "http://api.open-notify.org/astros.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        int choice;

        do {
            System.out.println("Pobierz położenie ISS");
            System.out.println("Pobierz ludzi na ISS");
            System.out.println("Zakończ aplikację");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    //  sprawdź położenie ISS
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ISS_API_LOCATION)).build();

                    final HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Tworzymy mappera, żeby wartości, których potrzebujemy z zewnętrznego serwisu
                    ObjectMapper objectMapper = new ObjectMapper();

                    final JsonNode jsonNode = objectMapper.readTree(send.body());

                    // wyciągamy timestamp jako long
                    long timestamp = jsonNode.at("/timestamp").asLong();

                    // Tworzymy obiekt obiekt Instant, który bęzie nam potrzebny do stworzenia dalej obiektu LocalDateTime
                    Instant instant = Instant.ofEpochSecond(timestamp);
                    LocalDateTime localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                    //  Wyciągamy lan i lat z jsona
                    final double lat = jsonNode.at("/iss_position/latitude").asDouble();
                    final double lon = jsonNode.at("/iss_position/longitude").asDouble();


                    System.out.println("Dnia " + localDate + " ISS " + "jest w miejscu: szerokość: " + lat + " długość: " + lon);

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("iss_location.csv", true))) {
                        StringBuilder line = new StringBuilder();
                        line.append("date").append(",").append(localDate).append(",").append("lat").append(",").append(lat).append(",").append("lon").append(",").append(lon).append("\n");
                        writer.write(line.toString());
                    }
                    break;
                case 2:
                    HttpClient client1 = HttpClient.newHttpClient();
                    HttpRequest request1 = HttpRequest.newBuilder().uri(URI.create(ISS_API_PEOPLE)).build();

                    HttpResponse<String> send1 = client1.send(request1, HttpResponse.BodyHandlers.ofString());

                    ObjectMapper objectMapper1 = new ObjectMapper();

                    final JsonNode jsonNode1 = objectMapper1.readTree(send1.body());

                    JsonNode numberOfPeople = jsonNode1.at("/number");

                    StringBuilder people = new StringBuilder();

                    for (JsonNode jsonArrayNode : jsonNode1.withArray("/people")) {
                        String name = jsonArrayNode.at("/name").asText();
                        System.out.println(name);
                        people.append(name).append(",");
                    }

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("iss_people.csv"))) {
                        writer.write(people.toString());
                    }
                    System.out.println("Wszystkich osób jest: " + numberOfPeople + "\n");

                    break;
                case 3:
                    System.out.println("Zamykamy appkę");
            }
        } while (choice != 3);
        scanner.close();

    }
}















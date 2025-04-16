package com.dharbor.sales.services;

import com.dharbor.sales.clients.NotificationsFeignClient;
import com.dharbor.sales.clients.RickMortyApiFeignClient;
import com.dharbor.sales.clients.StockFeignClient;
import com.dharbor.sales.clients.UsersFeignClient;
import com.dharbor.sales.exceptions.InternalServerErrorException;
import com.dharbor.sales.exceptions.SaleNotCompletedException;
import com.dharbor.sales.model.User;
import com.dharbor.sales.model.dto.NewSaleDto;
import com.dharbor.sales.model.rest.Character;
import com.dharbor.sales.model.rest.ProductReservationRequest;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewSalesService {

    private final UsersFeignClient usersFeignClient;

    private final StockFeignClient stockFeignClient;

    private final NotificationsFeignClient notificationsFeignClient;

    private final RickMortyApiFeignClient rickMortyApiFeignClient;

    @CircuitBreaker(name = "saleService", fallbackMethod = "newSaleFallback")
    @Retry(name = "saleService")
    public String newSale(NewSaleDto newSaleDto) throws SaleNotCompletedException {

        try {
            // Get the user
            User user = this.usersFeignClient.findById(newSaleDto.getUserId());

            // Create a new reservation
            ProductReservationRequest reservationRequest = new ProductReservationRequest();
            reservationRequest.setQuantity(newSaleDto.getQuantity());
            reservationRequest.setProductId(newSaleDto.getProductId());
            String reservationId = this.stockFeignClient.reserve(reservationRequest);

            // Send a new notification
            String notification = this.notificationsFeignClient
                    .sendNotification(newSaleDto.getUserId().toString());

            Character character = this.rickMortyApiFeignClient.findById(2);

            System.out.println(character.getName() + " " + character.getSpecies() + " " + character.getStatus());

            return "New Sale for " + user.getName() +
                    " with reservation id " + reservationId +
                    " and user has been notified " + notification;
        } catch (FeignException feignException) {
            throw new SaleNotCompletedException(feignException.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String newSaleFallback(NewSaleDto newSaleDto, Throwable throwable) {
        System.err.println("Fallback triggered for newSale: " + throwable.getMessage());
        return "Sale service temporarily unavailable. Please try again later. (Fallback)";
    }

    public String usersFallback(Long userId, Throwable throwable) {
        System.err.println("Fallback triggered for Users Feign Client: " + throwable.getMessage());
        return "User Service temporarily unavailable. Please try again later. (Users Fallback)";
    }

    public String stockFallback(ProductReservationRequest productReservationRequest, Throwable throwable) {
        System.err.println("Fallback triggered for Stock Feign Client: " + throwable.getMessage());
        return "Stock service temporarily unavailable. Please try again later. (Stock Fallback)";
    }

    public String notificationFallback(String userID, Throwable throwable) {
        System.err.println("Fallback triggered for notification Feign Client: " + throwable.getMessage());
        return "Notification service temporarily unavailable. Please try again later. (Notification Fallback)";
    }

    public Character rickMortyFallback(int id, Throwable throwable) {
        System.err.println("Fallback triggered for Rick and Morty Feign Client: " + throwable.getMessage());

        Character character = new Character();
        character.setName("Fallback Character");
        character.setSpecies("Fallback Species");
        character.setStatus("Fallback Status");

        return character;
    }
}

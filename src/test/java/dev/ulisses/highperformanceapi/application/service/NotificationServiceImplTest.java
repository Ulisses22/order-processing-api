package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.gateway.EmailNotificationSender;
import dev.ulisses.highperformanceapi.application.service.impl.NotificationServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import dev.ulisses.highperformanceapi.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailNotificationSender emailNotificationSender;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                emailNotificationSender
        );
    }

    @Test
    void shouldCreateNotification() {

        Customer customer = new Customer();
        Order order = new Order();

        Notification savedNotification = new Notification();
        savedNotification.setCustomer(customer);
        savedNotification.setOrder(order);
        savedNotification.setType(NotificationType.EMAIL);
        savedNotification.setStatus(NotificationStatus.PENDING);
        savedNotification.setSubject("Order created");
        savedNotification.setMessage("Your order has been created successfully.");

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification);

        Notification result = notificationService.create(
                customer,
                order,
                "Order created",
                "Your order has been created successfully."
        );

        assertThat(result).isSameAs(savedNotification);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldCreateEmailNotification() {

        Customer customer = new Customer();
        Order order = new Order();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.create(
                customer,
                order,
                "Order created",
                "Your order has been created successfully."
        );

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertThat(notification.getType())
                .isEqualTo(NotificationType.EMAIL);
    }

    @Test
    void shouldCreateNotificationWithPendingStatus() {

        Customer customer = new Customer();
        Order order = new Order();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.create(
                customer,
                order,
                "Order created",
                "Your order has been created successfully."
        );

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertThat(notification.getStatus())
                .isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void shouldSaveCustomerOrderSubjectAndMessage() {

        Customer customer = new Customer();
        Order order = new Order();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.create(
                customer,
                order,
                "Order created",
                "Your order has been created successfully."
        );

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertThat(notification.getCustomer())
                .isSameAs(customer);

        assertThat(notification.getOrder())
                .isSameAs(order);

        assertThat(notification.getSubject())
                .isEqualTo("Order created");

        assertThat(notification.getMessage())
                .isEqualTo("Your order has been created successfully.");
    }

    @Test
    void shouldMarkNotificationAsSentWhenEmailIsSentSuccessfully() {

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.send(notification);

        assertThat(notification.getStatus())
                .isEqualTo(NotificationStatus.SENT);

        verify(emailNotificationSender).send(notification);
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldMarkNotificationAsFailedWhenEmailSendingFails() {

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailNotificationSender)
                .send(notification);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.send(notification);

        assertThat(notification.getStatus())
                .isEqualTo(NotificationStatus.FAILED);

        verify(emailNotificationSender).send(notification);
        verify(notificationRepository).save(notification);
    }
}

package ru.servicecenter.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.*;
import ru.servicecenter.server.domain.enums.RepairStatus;
import ru.servicecenter.server.domain.enums.RoleName;
import ru.servicecenter.server.repository.*;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_MARKER_PHONE = "+79001000001";

    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final BrandRepository brandRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (clientRepository.findAll().stream().anyMatch(c -> DEMO_MARKER_PHONE.equals(c.getPhone()))) {
            return;
        }
        seedStaff();
        List<Client> clients = seedClients();
        List<Device> devices = seedDevices(clients);
        seedRepairs(clients, devices);
        log.info("Демо-данные: 30 клиентов, 10 заявок, 3 мастера (+ manager, manager2)");
    }

    private void seedStaff() {
        Role managerRole = roleRepository.findByName(RoleName.MANAGER).orElseThrow();
        Role masterRole = roleRepository.findByName(RoleName.MASTER).orElseThrow();

        createUserIfAbsent("manager2", "manager123", "Анна Менеджерова", "manager2@servicecenter.local", managerRole);

        createUserIfAbsent("master2", "master123", "Алексей Ремонтов", "master2@servicecenter.local", masterRole);
        createUserIfAbsent("master3", "master123", "Дмитрий Сервисов", "master3@servicecenter.local", masterRole);
    }

    private void createUserIfAbsent(String username, String password, String fullName, String email, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .email(email)
                    .active(true)
                    .role(role)
                    .build());
        }
    }

    private List<Client> seedClients() {
        String[] firstNames = {
                "Иван", "Мария", "Алексей", "Елена", "Дмитрий", "Ольга", "Сергей", "Анна", "Павел", "Наталья",
                "Андрей", "Татьяна", "Михаил", "Екатерина", "Николай", "Юлия", "Владимир", "Ирина", "Константин", "Светлана",
                "Роман", "Виктория", "Георгий", "Алина", "Максим", "Полина", "Артём", "Ксения", "Игорь", "Лариса"
        };
        String[] lastNames = {
                "Иванов", "Петрова", "Сидоров", "Козлова", "Новиков", "Морозова", "Волков", "Соколова", "Лебедев", "Павлова",
                "Кузнецов", "Орлова", "Смирнов", "Фёдорова", "Попов", "Васильева", "Семёнов", "Голубева", "Егоров", "Зайцева",
                "Белов", "Комарова", "Тихонов", "Борисова", "Андреев", "Киселёва", "Макаров", "Никитина", "Захаров", "Степанова"
        };

        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String phone = i == 0 ? DEMO_MARKER_PHONE : String.format("+79001%07d", i + 1);
            Client client = Client.builder()
                    .fullName(firstNames[i] + " " + lastNames[i])
                    .phone(phone)
                    .email("client" + (i + 1) + "@example.com")
                    .address("г. Москва, ул. Примерная, д. " + (i + 10))
                    .notes(i % 5 == 0 ? "Постоянный клиент" : null)
                    .build();
            clients.add(clientRepository.save(client));
        }
        return clients;
    }

    private List<Device> seedDevices(List<Client> clients) {
        List<DeviceType> types = deviceTypeRepository.findAll();
        List<Brand> brands = brandRepository.findAll();
        if (types.isEmpty() || brands.isEmpty()) {
            return List.of();
        }

        String[] models = {
                "Galaxy A54", "iPhone 14", "Redmi Note 12", "LG OLED55", "MacBook Air M2",
                "iPad 10", "Bosch WAT284", "Sony Bravia", "Realme 11", "Philips 55PUS"
        };
        String[] problems = {"экран", "батарея", "материнская плата", "корпус", "ПО"};

        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            Client client = clients.get(i);
            DeviceType type = types.get(i % types.size());
            Brand brand = brands.get(i % brands.size());
            Device device = Device.builder()
                    .client(client)
                    .deviceType(type.getName())
                    .brand(brand.getName())
                    .model(models[i % models.length])
                    .serialNumber("SN-" + Year.now().getValue() + "-" + String.format("%05d", i + 1))
                    .description("Устройство клиента — " + problems[i % problems.length])
                    .build();
            devices.add(deviceRepository.save(device));
        }
        return devices;
    }

    private void seedRepairs(List<Client> clients, List<Device> devices) {
        if (devices.isEmpty()) {
            return;
        }

        List<User> masters = userRepository.findByRole_Name(RoleName.MASTER);
        if (masters.isEmpty()) {
            return;
        }

        RepairStatus[] statuses = {
                RepairStatus.NEW,
                RepairStatus.IN_PROGRESS,
                RepairStatus.IN_PROGRESS,
                RepairStatus.WAITING_PARTS,
                RepairStatus.WAITING_PARTS,
                RepairStatus.COMPLETED,
                RepairStatus.COMPLETED,
                RepairStatus.COMPLETED,
                RepairStatus.CANCELED,
                RepairStatus.NEW
        };

        String[] problems = {
                "Не включается после падения",
                "Разбит экран, сенсор работает частично",
                "Не заряжается, разъём расшатан",
                "Нет изображения, звук есть",
                "Сильно шумит при стирке",
                "Не подключается к Wi‑Fi",
                "Перегревается и выключается",
                "Не открывается крышка/люк",
                "Клиент отказался от ремонта — дорого",
                "Попала влага, не реагирует на кнопки"
        };

        String[] diagnoses = {
                null,
                "Требуется замена модуля дисплея",
                "Замена разъёма питания",
                "Неисправна подсветка / T-Con",
                "Износ подшипника",
                "Прошивка сброшена, нужна настройка",
                "Замена термопасты и чистка",
                "Замена привода",
                null,
                null
        };

        BigDecimal[] estimated = {
                new BigDecimal("3500"), new BigDecimal("8900"), new BigDecimal("2200"),
                new BigDecimal("12000"), new BigDecimal("4500"), new BigDecimal("1500"),
                new BigDecimal("2800"), new BigDecimal("3100"), new BigDecimal("0"),
                new BigDecimal("5500")
        };

        BigDecimal[] finalCosts = {
                null, null, new BigDecimal("2400"), null, null,
                new BigDecimal("1500"), new BigDecimal("3000"), new BigDecimal("3200"),
                null, null
        };

        int year = Year.now().getValue();
        String prefix = "SC-" + year + "-";
        int nextNumber = repairRequestRepository.findMaxRequestSuffix(prefix) + 1;
        for (int i = 0; i < 10; i++) {
            String requestNumber;
            do {
                requestNumber = prefix + String.format("%05d", nextNumber);
                nextNumber++;
            } while (repairRequestRepository.existsByRequestNumber(requestNumber));

            User master = masters.get(i % masters.size());
            RepairRequest repair = RepairRequest.builder()
                    .requestNumber(requestNumber)
                    .client(clients.get(i))
                    .device(devices.get(i))
                    .master(master)
                    .status(statuses[i])
                    .problemDescription(problems[i])
                    .diagnosis(diagnoses[i])
                    .estimatedCost(estimated[i])
                    .finalCost(finalCosts[i])
                    .build();

            RepairRequest saved = repairRequestRepository.save(repair);
            serviceHistoryRepository.save(ServiceHistory.builder()
                    .repairRequest(saved)
                    .oldStatus(null)
                    .newStatus(RepairStatus.NEW)
                    .comment("Демо-заявка создана")
                    .build());

            if (statuses[i] != RepairStatus.NEW) {
                serviceHistoryRepository.save(ServiceHistory.builder()
                        .repairRequest(saved)
                        .oldStatus(RepairStatus.NEW)
                        .newStatus(statuses[i])
                        .comment("Статус обновлён при загрузке демо-данных")
                        .changedBy(master)
                        .build());
            }
        }
    }
}

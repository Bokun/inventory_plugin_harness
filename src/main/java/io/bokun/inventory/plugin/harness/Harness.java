package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import com.google.common.collect.*;
import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;
import io.bokun.inventory.common.api.grpc.Date;
import io.bokun.inventory.plugin.api.grpc.*;
import org.slf4j.*;

import static io.bokun.inventory.common.api.grpc.CancelBookingResponse.CancellationResultCase.SUCCESSFULCANCELLATION;
import static io.bokun.inventory.common.api.grpc.ConfirmBookingResponse.BookingResultCase.SUCCESSFULBOOKING;
import static io.bokun.inventory.common.api.grpc.Gender.*;
import static io.bokun.inventory.common.api.grpc.MeetingType.*;
import static io.bokun.inventory.common.api.grpc.PluginCapability.*;
import static io.bokun.inventory.common.api.grpc.ReservationResponse.ReservationResultCase.*;
import static java.lang.Math.*;

/**
 * Orchestrates end-to-end testing of target plugin.
 *
 * @author Mindaugas Žakšauskas
 */
public class Harness {

    private static final Logger log = LoggerFactory.getLogger(Harness.class);

    private final GetDefinitionAction getDefinitionAction;
    private final ConfigurePluginAction configurePluginAction;
    private final SearchProductsAction searchProductsAction;
    private final GetProductByIdAction getProductByIdAction;
    private final ShallowAvailabilityAction shallowAvailabilityAction;
    private final DeepAvailabilityAction deepAvailabilityAction;
    private final CreateReservationAction createReservationAction;
    private final ConfirmBookingAction confirmBookingAction;
    private final CreateAndConfirmBookingAction createAndConfirmBookingAction;
    private final CancelBookingAction cancelBookingAction;

    private final Random prng = new Random(System.nanoTime());

    @Inject
    public Harness(GetDefinitionAction getDefinitionAction,
                   ConfigurePluginAction configurePluginAction,
                   SearchProductsAction searchProductsAction,
                   GetProductByIdAction getProductByIdAction,
                   ShallowAvailabilityAction shallowAvailabilityAction,
                   DeepAvailabilityAction deepAvailabilityAction,
                   CreateReservationAction createReservationAction,
                   ConfirmBookingAction confirmBookingAction,
                   CreateAndConfirmBookingAction createAndConfirmBookingAction,
                   CancelBookingAction cancelBookingAction) {
        this.getDefinitionAction = getDefinitionAction;
        this.configurePluginAction = configurePluginAction;
        this.searchProductsAction = searchProductsAction;
        this.getProductByIdAction = getProductByIdAction;
        this.shallowAvailabilityAction = shallowAvailabilityAction;
        this.deepAvailabilityAction = deepAvailabilityAction;
        this.createReservationAction = createReservationAction;
        this.confirmBookingAction = confirmBookingAction;
        this.createAndConfirmBookingAction = createAndConfirmBookingAction;
        this.cancelBookingAction = cancelBookingAction;
    }

    private Date dateYearLater() {
        LocalDate now = LocalDate.now();
        return Date.newBuilder()
                .setYear(now.getYear() + 1)
                .setMonth(now.getMonthValue())
                .setDay(min(28, now.getDayOfMonth()))
                .build();
    }

    private <T> T getRandomElement(Iterable<T> iterable) {
        int size = Iterables.size(iterable);
        return Iterables.get(iterable, prng.nextInt(size));
    }

    private Contact createFakeContact(Iterable<ContactField> enforcedFields) {
        Contact.Builder result = Contact.newBuilder()
                .setFirstName("Bókun test - first name")
                .setLastName("Bókun test - last name");
        for (ContactField enforcedField : enforcedFields) {
            switch (enforcedField) {
                case ADDRESS: result.setAddress("123 Total satisfaction street London"); break;
                case COUNTRY: result.setCountry("GB"); break;
                case EMAIL: result.setEmail("test@bokun.io"); break;
                case FIRST_NAME: break;     // already added by default
                case GENDER: result.setGender(MALE); break;
                case LANGUAGE: result.setLanguage("EN"); break;
                case LAST_NAME: break;      // already added by default
                case NATIONALITY: result.setNationality("GB"); break;
                case ORGANIZATION: result.setOrganization("Bokun bhf."); break;
                case PASSPORT_EXPIRY: result.setPassportExpiry(dateYearLater()); break;
                case PASSPORT_NUMBER: result.setPassportNumber("123454678"); break;
                case PHONE: result.setPhone("+447890123456"); break;
                case POST_CODE: result.setPostCode("SW1A 1AA"); break;
                case TITLE: result.setTitle(Title.MR); break;
                case UNASSIGNED_FIELD:
                case UNRECOGNIZED:
                default: throw new UnsupportedOperationException("Unsupported field: " + enforcedField);
            }
        }
        return result.build();
    }

    private Passenger createPassenger(Contact contact, RateWithPrice rateWithPrice) {
        Passenger.Builder result = Passenger.newBuilder()
                .setContact(contact);
        if (rateWithPrice.getPricingOptionsCase() == RateWithPrice.PricingOptionsCase.PRICEPERPERSON) {
            PricePerPerson pricePerPerson = rateWithPrice.getPricePerPerson();
            PricingCategoryWithPrice pricingCategoryWithPrice = getRandomElement(pricePerPerson.getPricingCategoryWithPriceList());
            result.setPricePerPassenger(pricingCategoryWithPrice.getPrice());
            result.setPricingCategoryId(pricingCategoryWithPrice.getPricingCategoryId());
        } else {
            throw new UnsupportedOperationException("Price per booking is not yet supported by this test harness");
        }
        return result.build();
    }

    private ReservationData prepareReservationData(ProductDescription productDescription, ProductAvailabilityWithRatesResponse dateWithRate) {
        Contact leadPassenger = createFakeContact(productDescription.getEnforcedLeadPassengerFieldsList());
        ReservationData.Builder result = ReservationData.newBuilder()
                .setProductId(productDescription.getId())
                .setCustomerContact(leadPassenger)
                .setNotes("Test notes")
                .setDate(dateWithRate.getDate());
        if (dateWithRate.hasTime()) {
            result.setTime(dateWithRate.getTime());
        }
        if ((productDescription.getMeetingType() == PICK_UP) || (productDescription.getMeetingType() == MEET_ON_LOCATION_OR_PICK_UP)) {
            result.setPickupRequired(true);
            if (productDescription.getCustomPickupPlaceAllowed()) {
                result.setCustomPickupPlace("Some custom pickup place");
            } else {
                result.setPredefinedPickupPlace(getRandomElement(productDescription.getPickupPlacesList()));
            }
            result.setDropoffRequired(false);
        }
        RateWithPrice randomRate = getRandomElement(dateWithRate.getRatesList());
        result.addReservations(
                Reservation.newBuilder()
                        .setRateId(randomRate.getRateId())
                        .addPassengers(createPassenger(leadPassenger, randomRate))
        );
        result.setPlatformId(Integer.toString(abs(prng.nextInt())));
        return result.build();
    }

    public void runEndToEnd(Main.Configuration configuration) {
        // step 1: get and validate plugin definition
        PluginDefinition pluginDefinition = getDefinitionAction.getDefinition(configuration.pluginUrl);
        log.info("Received definition for plugin {}", pluginDefinition.getName());
        log.debug("Definition: {}", pluginDefinition);

        // step 2: not really calling the plugin, but instead transforming environment variables into a list of plugin configuration parameters
        Collection<PluginConfigurationParameterValue> pluginConfiguration =
                configurePluginAction.getPluginConfigurationParameterValues(pluginDefinition);
        log.info("Received definition for plugin {}", pluginDefinition.getName());

        // step 3: searching for all products this plugin may provide. Supply configuration values built in the previous step.
        List<BasicProductInfo> basicProducts = searchProductsAction.search(configuration.pluginUrl, pluginConfiguration);
        log.info("Received total of {} products", basicProducts.size());

        // step 4: make a shallow call for availabilities on a small range of products until we find availability
        LocalDate today = LocalDate.now();
        LocalDate monthLater = today.plusMonths(1L);
        List<String> allProductIds = basicProducts.stream()
                .map(BasicProductInfo::getId)
                .collect(Collectors.toList());
        Set<String> availableProducts;
        do {
            Collection<String> randomThree = new ArrayList<>();
            for (int i=0; i<3 && !allProductIds.isEmpty(); i++) {
                int randomIndex = prng.nextInt(allProductIds.size());
                randomThree.add(allProductIds.get(randomIndex));
                allProductIds.remove(randomIndex);
            }
            if (randomThree.isEmpty()) {
                log.error("Could not find products to check availability for.");
                return;
            }

            availableProducts = shallowAvailabilityAction.getAvailableProducts(configuration.pluginUrl, pluginConfiguration, today, monthLater, 1, randomThree);
        } while (availableProducts.isEmpty());
        String randomAvailableProductId = Iterables.get(availableProducts, prng.nextInt(availableProducts.size()));
        log.info("Will inquiry and make bookings for product id={}", randomAvailableProductId);

        // step 5: call getProductById on selected random product which has availability over next month and verify/inspect it
        ProductDescription product = getProductByIdAction.getProductById(configuration.pluginUrl, pluginConfiguration, randomAvailableProductId);
        log.info("Inquiry for product {} was successful: {}", randomAvailableProductId, product);

        // step 6: make deep availability call and get some pricing info.
        List<ProductAvailabilityWithRatesResponse> deepAvailability = deepAvailabilityAction.getAvailability(
                configuration.pluginUrl,
                pluginConfiguration,
                today,
                monthLater,
                product.getId()
        );
        if (deepAvailability.isEmpty()) {
            log.error("No deep availability found even though shallow availability result was positive");
            return;
        }
        log.info("Found availabilities for product {}: {}", randomAvailableProductId, deepAvailability);

        // Step 7a + 7b: if system supports reservations, reserve and make booking
        // Otherwise, step 7c: if system doesn't support reservations, reserve and make booking in one step
        ProductAvailabilityWithRatesResponse randomAvailabilityWithRates = getRandomElement(deepAvailability);
        ReservationData reservationData = prepareReservationData(product, randomAvailabilityWithRates);
        ConfirmationData confirmationData = ConfirmationData.newBuilder()
                .setTicketSupport(getRandomElement(product.getTicketSupportList()))
                .build();
        ConfirmBookingResponse confirmBookingResponse;
        if (pluginDefinition.getCapabilitiesList().contains(SUPPORTS_RESERVATIONS)) {
            ReservationRequest reservationRequest = ReservationRequest.newBuilder()
                    .setReservationData(reservationData)
                    .addAllParameters(pluginConfiguration)
                    .build();
            ReservationResponse reservationResponse = createReservationAction.createReservation(configuration.pluginUrl, reservationRequest);
            if (reservationResponse.getReservationResultCase() != SUCCESSFULRESERVATION) {
                log.error("Could not make successful reservation");
                return;
            }
            log.info("Successfully reserved booking {}", reservationResponse);
            ConfirmBookingRequest confirmBookingRequest = ConfirmBookingRequest.newBuilder()
                    .addAllParameters(pluginConfiguration)
                    .setReservationConfirmationCode(reservationResponse.getSuccessfulReservation().getReservationConfirmationCode())
                    .setReservationData(reservationData)
                    .setConfirmationData(
                            confirmationData
                    )
                    .build();
            confirmBookingResponse = confirmBookingAction.confirmBooking(configuration.pluginUrl, confirmBookingRequest);
        } else {
            CreateConfirmBookingRequest createConfirmRequest = CreateConfirmBookingRequest.newBuilder()
                    .addAllParameters(pluginConfiguration)
                    .setReservationData(reservationData)
                    .setConfirmationData(confirmationData)
                    .build();
            confirmBookingResponse = createAndConfirmBookingAction.createAndConfirmBooking(configuration.pluginUrl, createConfirmRequest);
        }
        if (confirmBookingResponse.getBookingResultCase() != SUCCESSFULBOOKING) {
            log.error("Could not successfully confirm booking");
            return;
        }
        log.info("Successfully confirmed booking {}", confirmBookingResponse);

        // step 8: cancel confirmed booking
        CancelBookingRequest cancelBookingRequest = CancelBookingRequest.newBuilder()
                .addAllParameters(pluginConfiguration)
                .setBookingConfirmationCode(confirmBookingResponse.getSuccessfulBooking().getBookingConfirmationCode())
                .build();
        CancelBookingResponse cancelBookingResponse = cancelBookingAction.cancelBooking(configuration.pluginUrl, cancelBookingRequest);
        if (cancelBookingResponse.getCancellationResultCase() != SUCCESSFULCANCELLATION) {
            log.error("Could not successfully cancel booking");
            return;
        }
        log.info("Successfully cancelled booking {}", cancelBookingResponse);
        log.info("Exiting...");
    }
}

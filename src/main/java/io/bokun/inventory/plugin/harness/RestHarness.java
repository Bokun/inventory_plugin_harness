package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import com.google.common.collect.*;
import com.google.inject.*;
import io.bokun.inventory.plugin.api.rest.*;
import org.slf4j.*;

/**
 * Orchestrates end-to-end testing of target plugin.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestHarness {

    private static final Logger log = LoggerFactory.getLogger(RestHarness.class);

    private final RestGetDefinitionAction restGetDefinitionAction;
    private final RestConfigurePluginAction restConfigurePluginAction;
    private final RestSearchProductsAction restSearchProductsAction;
    private final RestGetProductByIdAction restGetProductByIdAction;
    private final RestShallowAvailabilityAction restShallowAvailabilityAction;
    private final GrpcDeepAvailabilityAction grpcDeepAvailabilityAction;
    private final GrpcCreateReservationAction grpcCreateReservationAction;
    private final GrpcConfirmBookingAction grpcConfirmBookingAction;
    private final GrpcCreateAndConfirmBookingAction grpcCreateAndConfirmBookingAction;
    private final GrpcCancelBookingAction grpcCancelBookingAction;

    private final Random prng = new Random(System.nanoTime());

    @Inject
    public RestHarness(RestGetDefinitionAction restGetDefinitionAction,
                       RestConfigurePluginAction restConfigurePluginAction,
                       RestSearchProductsAction restSearchProductsAction,
                       RestGetProductByIdAction restGetProductByIdAction,
                       RestShallowAvailabilityAction restShallowAvailabilityAction,
                       GrpcDeepAvailabilityAction grpcDeepAvailabilityAction,
                       GrpcCreateReservationAction grpcCreateReservationAction,
                       GrpcConfirmBookingAction grpcConfirmBookingAction,
                       GrpcCreateAndConfirmBookingAction grpcCreateAndConfirmBookingAction,
                       GrpcCancelBookingAction grpcCancelBookingAction) {
        this.restGetDefinitionAction = restGetDefinitionAction;
        this.restConfigurePluginAction = restConfigurePluginAction;
        this.restSearchProductsAction = restSearchProductsAction;
        this.restGetProductByIdAction = restGetProductByIdAction;
        this.restShallowAvailabilityAction = restShallowAvailabilityAction;
        this.grpcDeepAvailabilityAction = grpcDeepAvailabilityAction;
        this.grpcCreateReservationAction = grpcCreateReservationAction;
        this.grpcConfirmBookingAction = grpcConfirmBookingAction;
        this.grpcCreateAndConfirmBookingAction = grpcCreateAndConfirmBookingAction;
        this.grpcCancelBookingAction = grpcCancelBookingAction;
    }

    private <T> T getRandomElement(Iterable<T> iterable) {
        int size = Iterables.size(iterable);
        return Iterables.get(iterable, prng.nextInt(size));
    }

    private DateYMD oneYearLater() {
        LocalDate afterOneYear = LocalDate.now().plusYears(1L);
        DateYMD out = new DateYMD();
        out.setYear(afterOneYear.getYear());
        out.setMonth(afterOneYear.getMonth().getValue());
        out.setDay(afterOneYear.getDayOfMonth());
        return out;
    }

    private Contact createFakeContact(Iterable<ContactField> enforcedFields) {
        Contact result = new Contact();
        result.setFirstName("Bókun test - first name");
        result.setLastName("Bókun test - last name");
        for (ContactField enforcedField : enforcedFields) {
            switch (enforcedField) {
                case ADDRESS: result.setAddress("123 Total satisfaction street London"); break;
                case COUNTRY: result.setCountry("GB"); break;
                case EMAIL: result.setEmail("test@bokun.io"); break;
                case FIRST_NAME: break;     // already added by default
                case GENDER: result.setGender(Gender.MALE); break;
                case LANGUAGE: result.setLanguage("EN"); break;
                case LAST_NAME: break;      // already added by default
                case NATIONALITY: result.setNationality("GB"); break;
                case ORGANIZATION: result.setOrganization("Bokun bhf."); break;
                case PASSPORT_EXPIRY: result.setPassportExpiry(oneYearLater()); break;
                case PASSPORT_NUMBER: result.setPassportNumber("123454678"); break;
                case PHONE: result.setPhone("+447890123456"); break;
                case POST_CODE: result.setPostCode("SW1A 1AA"); break;
                case TITLE: result.setTitle(Title.MR); break;
                default: throw new UnsupportedOperationException("Unsupported field: " + enforcedField);
            }
        }
        return result;
    }

//    private Passenger createPassenger(Contact contact, RateWithPrice rateWithPrice) {
//        Passenger.Builder result = Passenger.newBuilder()
//                .setContact(contact);
//        if (rateWithPrice.getPricingOptionsCase() == RateWithPrice.PricingOptionsCase.PRICEPERPERSON) {
//            PricePerPerson pricePerPerson = rateWithPrice.getPricePerPerson();
//            PricingCategoryWithPrice pricingCategoryWithPrice = getRandomElement(pricePerPerson.getPricingCategoryWithPriceList());
//            result.setPricePerPassenger(pricingCategoryWithPrice.getPrice());
//            result.setPricingCategoryId(pricingCategoryWithPrice.getPricingCategoryId());
//        } else {
//            throw new UnsupportedOperationException("Price per booking is not yet supported by this test harness");
//        }
//        return result.build();
//    }

//    private ReservationData prepareReservationData(ProductDescription productDescription, ProductAvailabilityWithRatesResponse dateWithRate) {
//        Contact leadPassenger = createFakeContact(productDescription.getEnforcedLeadPassengerFieldsList());
//        ReservationData.Builder result = ReservationData.newBuilder()
//                .setProductId(productDescription.getId())
//                .setCustomerContact(leadPassenger)
//                .setNotes("Test notes")
//                .setDate(dateWithRate.getDate());
//        if (dateWithRate.hasTime()) {
//            result.setTime(dateWithRate.getTime());
//        }
//        if ((productDescription.getMeetingType() == PICK_UP) || (productDescription.getMeetingType() == MEET_ON_LOCATION_OR_PICK_UP)) {
//            result.setPickupRequired(true);
//            if (productDescription.getCustomPickupPlaceAllowed()) {
//                result.setCustomPickupPlace("Some custom pickup place");
//            } else {
//                result.setPredefinedPickupPlace(getRandomElement(productDescription.getPickupPlacesList()));
//            }
//            result.setDropoffRequired(false);
//        }
//        RateWithPrice randomRate = getRandomElement(dateWithRate.getRatesList());
//        result.addReservations(
//                Reservation.newBuilder()
//                        .setRateId(randomRate.getRateId())
//                        .addPassengers(createPassenger(leadPassenger, randomRate))
//        );
//        result.setPlatformId(Integer.toString(abs(prng.nextInt())));
//        result.setBookingSource(prepareBookingSourceData());
//        return result.build();
//    }
//
//    private BookingSource prepareBookingSourceData() {
//        List<SalesSegment> salesSegments = Arrays.stream(SalesSegment.values())
//                .filter(s-> s!=SalesSegment.UNASSIGNED_SALES_SEGMENT && s!=SalesSegment.UNRECOGNIZED)
//                .collect(Collectors.toList());
//        SalesSegment salesSegment = getRandomElement(salesSegments);
//        BookingSource.Builder bookingSource = BookingSource.newBuilder()
//                .setSegment(salesSegment);
//        BookingSourceBookingChannel.Builder bookingChannel = BookingSourceBookingChannel.newBuilder();
//        switch (salesSegment) {
//            case MARKETPLACE:
//                bookingChannel
//                    .setId("10001")
//                    .setTitle("Marketplace Booking Channel");
//                bookingSource.setMarketplaceVendor(BookingSourceMarketplaceVendor.newBuilder()
//                        .setId("10002")
//                        .setTitle("Test Reseller Vendor")
//                        .setCompanyRegistrationNumber("3025381111")
//                );
//                break;
//            case AGENT_AREA:
//                bookingChannel
//                        .setId("10004")
//                        .setTitle("Agent Booking Channel");
//                bookingSource.setBookingAgent(BookingSourceBookingAgent.newBuilder()
//                        .setId("10003")
//                        .setTitle("Test Booking Agent")
//                        .setCompanyRegistrationNumber("3025382222")
//                );
//                break;
//            case DIRECT_OFFLINE:
//                bookingChannel
//                        .setId("10005")
//                        .setTitle("Backend Booking Channel");
//                bookingSource.setExtranetUser(BookingSourceExtranetUser.newBuilder()
//                        .setEmail("test@test.com")
//                        .setFullName("Test User")
//                );
//                break;
//            case OTA:
//                bookingChannel
//                        .setId("10006")
//                        .setTitle("OTA Booking Channel")
//                        .setSystemType("TEST_OTA");
//                break;
//            case DIRECT_ONLINE:
//                bookingChannel
//                        .setId("10007")
//                        .setTitle("Test Booking Channel");
//                break;
//        }
//        bookingSource.setBookingChannel(bookingChannel);
//        return bookingSource.build();
//    }

    public void runEndToEnd(Main.Configuration configuration) {
        // step 1: get and validate plugin definition
        io.bokun.inventory.plugin.api.rest.PluginDefinition pluginDefinition = restGetDefinitionAction.getDefinition(configuration.pluginData);
        log.info("Received definition for plugin {}", pluginDefinition.getName());
        log.debug("Definition: {}", pluginDefinition);

        // step 2: not really calling the plugin, but instead transforming environment variables into a list of plugin configuration parameters
        Collection<PluginConfigurationParameterValue> pluginConfiguration =
                restConfigurePluginAction.getPluginConfigurationParameterValues(pluginDefinition);
        log.info("Received definition for plugin {}", pluginDefinition.getName());

        // step 3: searching for all products this plugin may provide. Supply configuration values built in the previous step.
        List<BasicProductInfo> basicProducts = restSearchProductsAction.search(configuration.pluginData, pluginConfiguration);
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

            availableProducts = restShallowAvailabilityAction.getAvailableProducts(configuration.pluginData, pluginConfiguration, today, monthLater, 1, randomThree);
        } while (availableProducts.isEmpty());
        String randomAvailableProductId = Iterables.get(availableProducts, prng.nextInt(availableProducts.size()));
        log.info("Will inquiry and make bookings for product id={}", randomAvailableProductId);

        // step 5: call getProductById on selected random product which has availability over next month and verify/inspect it
        ProductDescription product = restGetProductByIdAction.getProductById(configuration.pluginData, pluginConfiguration, randomAvailableProductId);
        log.info("Inquiry for product {} was successful: {}", randomAvailableProductId, product);
//
//        // step 6: make deep availability call and get some pricing info.
//        List<ProductAvailabilityWithRatesResponse> deepAvailability = grpcDeepAvailabilityAction.getAvailability(
//                configuration.pluginData,
//                pluginConfiguration,
//                today,
//                monthLater,
//                product.getId()
//        );
//        if (deepAvailability.isEmpty()) {
//            log.error("No deep availability found even though shallow availability result was positive");
//            return;
//        }
//        log.info("Found availabilities for product {}: {}", randomAvailableProductId, deepAvailability);
//
//        // Step 7a + 7b: if system supports reservations, reserve and make booking
//        // Otherwise, step 7c: if system doesn't support reservations, reserve and make booking in one step
//        ProductAvailabilityWithRatesResponse randomAvailabilityWithRates = getRandomElement(deepAvailability);
//        ReservationData reservationData = prepareReservationData(product, randomAvailabilityWithRates);
//        ConfirmationData confirmationData = ConfirmationData.newBuilder()
//                .setTicketSupport(getRandomElement(product.getTicketSupportList()))
//                .build();
//        ConfirmBookingResponse confirmBookingResponse;
//        if (pluginDefinition.getCapabilitiesList().contains(SUPPORTS_RESERVATIONS)) {
//            ReservationRequest reservationRequest = ReservationRequest.newBuilder()
//                    .setReservationData(reservationData)
//                    .addAllParameters(pluginConfiguration)
//                    .build();
//            ReservationResponse reservationResponse = grpcCreateReservationAction.createReservation(configuration.pluginData, reservationRequest);
//            if (reservationResponse.getReservationResultCase() != SUCCESSFULRESERVATION) {
//                log.error("Could not make successful reservation");
//                return;
//            }
//            log.info("Successfully reserved booking {}", reservationResponse);
//            ConfirmBookingRequest confirmBookingRequest = ConfirmBookingRequest.newBuilder()
//                    .addAllParameters(pluginConfiguration)
//                    .setReservationConfirmationCode(reservationResponse.getSuccessfulReservation().getReservationConfirmationCode())
//                    .setReservationData(reservationData)
//                    .setConfirmationData(
//                            confirmationData
//                    )
//                    .build();
//            confirmBookingResponse = grpcConfirmBookingAction.confirmBooking(configuration.pluginData, confirmBookingRequest);
//        } else {
//            CreateConfirmBookingRequest createConfirmRequest = CreateConfirmBookingRequest.newBuilder()
//                    .addAllParameters(pluginConfiguration)
//                    .setReservationData(reservationData)
//                    .setConfirmationData(confirmationData)
//                    .build();
//            confirmBookingResponse = grpcCreateAndConfirmBookingAction.createAndConfirmBooking(configuration.pluginData, createConfirmRequest);
//        }
//        if (confirmBookingResponse.getBookingResultCase() != SUCCESSFULBOOKING) {
//            log.error("Could not successfully confirm booking");
//            return;
//        }
//        log.info("Successfully confirmed booking {}", confirmBookingResponse);
//
//        // step 8: cancel confirmed booking
//        CancelBookingRequest cancelBookingRequest = CancelBookingRequest.newBuilder()
//                .addAllParameters(pluginConfiguration)
//                .setBookingConfirmationCode(confirmBookingResponse.getSuccessfulBooking().getBookingConfirmationCode())
//                .build();
//        CancelBookingResponse cancelBookingResponse = grpcCancelBookingAction.cancelBooking(configuration.pluginData, cancelBookingRequest);
//        if (cancelBookingResponse.getCancellationResultCase() != SUCCESSFULCANCELLATION) {
//            log.error("Could not successfully cancel booking");
//            return;
//        }
//        log.info("Successfully cancelled booking {}", cancelBookingResponse);
//        log.info("Exiting...");
    }
}

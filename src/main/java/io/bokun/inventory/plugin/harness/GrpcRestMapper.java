package io.bokun.inventory.plugin.harness;

import java.util.*;

import javax.annotation.*;

import com.google.protobuf.*;
import io.bokun.inventory.plugin.api.rest.*;

/**
 * Converts between various combinations of GRPC (both server and client) and REST representations of the model classes.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcRestMapper {

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private GrpcRestMapper() {
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PluginCapability restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PluginCapability in) {
        return io.bokun.inventory.common.api.grpc.PluginCapability.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PluginParameterDataType restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PluginParameterDataType in) {
        return io.bokun.inventory.common.api.grpc.PluginParameterDataType.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.plugin.api.grpc.PluginConfigurationParameter restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PluginConfigurationParameter in) {
        return io.bokun.inventory.plugin.api.grpc.PluginConfigurationParameter.newBuilder()
                .setName(in.getName())
                .setRequired(in.getRequired())
                .setType(restToGrpc(in.getType()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.plugin.api.grpc.PluginDefinition restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PluginDefinition in) {
        io.bokun.inventory.plugin.api.grpc.PluginDefinition.Builder result = io.bokun.inventory.plugin.api.grpc.PluginDefinition.newBuilder()
                .setName(in.getName());
        if (in.getDescription() != null) {
            result.setDescription(in.getDescription());
        }
        in.getCapabilities().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(result::addCapabilities);
        in.getParameters().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(result::addParameters);
        return result.build();
    }

    private static io.bokun.inventory.common.api.grpc.PricingCategory restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PricingCategory in) {
        io.bokun.inventory.common.api.grpc.PricingCategory.Builder result = io.bokun.inventory.common.api.grpc.PricingCategory.newBuilder()
                .setId(in.getId())
                .setLabel(in.getLabel());
        if (in.getMinAge() != null) {
            result.setMinAge(in.getMinAge());
        }
        if (in.getMaxAge() != null) {
            result.setMaxAge(in.getMaxAge());
        }
        return result.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.BasicProductInfo restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.BasicProductInfo in) {
        io.bokun.inventory.common.api.grpc.BasicProductInfo.Builder result = io.bokun.inventory.common.api.grpc.BasicProductInfo.newBuilder()
                .setId(in.getId())
                .setName(in.getName());
        if (in.getDescription() != null) {
            result.setDescription(in.getDescription());
        }
        if (in.getPricingCategories() != null) {
            in.getPricingCategories().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(result::addPricingCategories);
        }
        in.getCities().forEach(result::addCities);
        in.getCountries().forEach(result::addCountries);
        return result.build();
    }

    public static io.bokun.inventory.common.api.grpc.Rate restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Rate in) {
        return io.bokun.inventory.common.api.grpc.Rate.newBuilder()
                .setId(in.getId())
                .setLabel(in.getLabel())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Duration restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Duration in) {
        io.bokun.inventory.common.api.grpc.Duration.Builder result = io.bokun.inventory.common.api.grpc.Duration.newBuilder();
        if (in.getWeeks() != null) {
            result.setWeeks(in.getWeeks());
        }
        if (in.getDays() != null) {
            result.setDays(in.getDays());
        }
        if (in.getHours() != null) {
            result.setHours(in.getHours());
        }
        if (in.getMinutes() != null) {
            result.setMinutes(in.getMinutes());
        }
        return result.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.OpeningHoursTimeInterval restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.OpeningHoursTimeInterval in) {
        io.bokun.inventory.common.api.grpc.OpeningHoursTimeInterval.Builder result = io.bokun.inventory.common.api.grpc.OpeningHoursTimeInterval.newBuilder()
                .setOpenFrom(in.getOpenFrom())
                .setOpenForHours(in.getOpenForHours())
                .setOpenForMinutes(in.getOpenForMinutes());
        if (in.getDuration() != null) {
            result.setDuration(restToGrpc(in.getDuration()));
        }
        return result.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.OpeningHoursWeekday restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.OpeningHoursWeekday in) {
        io.bokun.inventory.common.api.grpc.OpeningHoursWeekday.Builder out = io.bokun.inventory.common.api.grpc.OpeningHoursWeekday.newBuilder()
                .setOpen24Hours(in.getOpen24Hours());
        if (in.getTimeIntervals() != null) {
            in.getTimeIntervals().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addTimeIntervals);
        }
        return out.build();
    }

    public static io.bokun.inventory.common.api.grpc.OpeningHours restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.OpeningHours in) {
        io.bokun.inventory.common.api.grpc.OpeningHours.Builder out = io.bokun.inventory.common.api.grpc.OpeningHours.newBuilder();
        if (in.getMonday() != null) {
            out.setMonday(restToGrpc(in.getMonday()));
        }
        if (in.getTuesday() != null) {
            out.setTuesday(restToGrpc(in.getTuesday()));
        }
        if (in.getWednesday() != null) {
            out.setWednesday(restToGrpc(in.getWednesday()));
        }
        if (in.getThursday() != null) {
            out.setThursday(restToGrpc(in.getThursday()));
        }
        if (in.getFriday() != null) {
            out.setFriday(restToGrpc(in.getFriday()));
        }
        if (in.getSaturday() != null) {
            out.setSaturday(restToGrpc(in.getSaturday()));
        }
        if (in.getSunday() != null) {
            out.setSunday(restToGrpc(in.getSunday()));
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.SeasonalOpeningHours restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.SeasonalOpeningHours in) {
        return io.bokun.inventory.common.api.grpc.SeasonalOpeningHours.newBuilder()
                .setStartMonth(in.getStartMonth())
                .setStartDay(in.getStartDay())
                .setEndMonth(in.getEndMonth())
                .setEndDay(in.getEndDay())
                .setOpeningHours(restToGrpc(in.getOpeningHours()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.BookingType restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.BookingType in) {
        return io.bokun.inventory.common.api.grpc.BookingType.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.GeoPoint restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.GeoPoint in) {
        return io.bokun.inventory.common.api.grpc.GeoPoint.newBuilder()
                .setLatitude(in.getLatitude())
                .setLongitude(in.getLongitude())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.UnLocode restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.UnLocode in) {
        return io.bokun.inventory.common.api.grpc.UnLocode.newBuilder()
                .setCountry(in.getCountry())
                .setCity(in.getCity())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Address restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Address in) {
        io.bokun.inventory.common.api.grpc.Address.Builder out = io.bokun.inventory.common.api.grpc.Address.newBuilder();
        if (in.getAddressLine1() != null) {
            out.setAddressLine1(in.getAddressLine1());
        }
        if (in.getAddressLine2() != null) {
            out.setAddressLine2(in.getAddressLine2());
        }
        if (in.getAddressLine3() != null) {
            out.setAddressLine3(in.getAddressLine3());
        }
        if (in.getCity() != null) {
            out.setCity(in.getCity());
        }
        if (in.getState() != null) {
            out.setState(in.getState());
        }
        if (in.getPostalCode() != null) {
            out.setPostalCode(in.getPostalCode());
        }
        if (in.getCountryCode() != null) {
            out.setCountryCode(in.getCountryCode());
        }
        if (in.getGeoPoint() != null) {
            out.setGeoPoint(restToGrpc(in.getGeoPoint()));
        }
        if (in.getUnLocode() != null) {
            out.setUnLocode(restToGrpc(in.getUnLocode()));
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PickupDropoffPlace restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PickupDropoffPlace in) {
        return io.bokun.inventory.common.api.grpc.PickupDropoffPlace.newBuilder()
                .setTitle(in.getTitle())
                .setAddress(restToGrpc(in.getAddress()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ProductCategory restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ProductCategory in) {
        return io.bokun.inventory.common.api.grpc.ProductCategory.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.TicketSupport restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.TicketSupport in) {
        return io.bokun.inventory.common.api.grpc.TicketSupport.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Time restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Time in) {
        return io.bokun.inventory.common.api.grpc.Time.newBuilder()
                .setHour(in.getHour())
                .setMinute(in.getMinute())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.TicketType restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.TicketType in) {
        return io.bokun.inventory.common.api.grpc.TicketType.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.MeetingType restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.MeetingType in) {
        return io.bokun.inventory.common.api.grpc.MeetingType.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ContactField restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ContactField in) {
        return io.bokun.inventory.common.api.grpc.ContactField.valueOf(in.getValue());
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Extra restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Extra in) {
        io.bokun.inventory.common.api.grpc.Extra.Builder out = io.bokun.inventory.common.api.grpc.Extra.newBuilder()
                .setId(in.getId())
                .setTitle(in.getTitle())
                .setOptional(in.getOptional());
        if (in.getDescription() != null) {
            out.setDescription(in.getDescription());
        }
        if (in.getMaxPerBooking() != null) {
            out.setMaxPerBooking(in.getMaxPerBooking());
        }
        if (in.getLimitByPax() != null) {
            out.setLimitByPax(in.getLimitByPax());
        }
        if (in.getIncreasesCapacity() != null) {
            out.setIncreasesCapacity(in.getIncreasesCapacity());
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ProductDescription restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ProductDescription in) {
        io.bokun.inventory.common.api.grpc.ProductDescription.Builder out = io.bokun.inventory.common.api.grpc.ProductDescription.newBuilder()
                .setId(in.getId())
                .setName(in.getName());
        if (in.getDescription() != null) {
            out.setDescription(in.getDescription());
        }
        in.getPricingCategories().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addPricingCategories);
        in.getRates().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addRates);
        if (in.getAllYearOpeningHours() != null) {
            out.setAllYearOpeningHours(restToGrpc(in.getAllYearOpeningHours()));
        } else if (in.getSeasonalOpeningHours() != null) {
            io.bokun.inventory.plugin.api.rest.SeasonalOpeningHourSet seasonalOpeningHoursIn = in.getSeasonalOpeningHours();
            io.bokun.inventory.common.api.grpc.SeasonalOpeningHourSet.Builder seasonalOpeningHoursOut = io.bokun.inventory.common.api.grpc.SeasonalOpeningHourSet.newBuilder();
            seasonalOpeningHoursIn.getSeasonalOpeningHours().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(seasonalOpeningHoursOut::addSeasonalOpeningHours);
            out.setSeasonalOpeningHours(seasonalOpeningHoursOut);
        }
        out.setBookingType(restToGrpc(in.getBookingType()));
        if (in.getMeetingType() == MeetingType.MEET_ON_LOCATION_OR_PICK_UP || in.getMeetingType() == MeetingType.PICK_UP) {
            out.setCustomPickupPlaceAllowed(in.getCustomPickupPlaceAllowed());
        }
        if (in.getPickupMinutesBefore() != null) {
            out.setPickupMinutesBefore(in.getPickupMinutesBefore());
        }
        if (in.getMeetingType() == MeetingType.MEET_ON_LOCATION_OR_PICK_UP || in.getMeetingType() == MeetingType.PICK_UP) {
            in.getPickupPlaces().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addPickupPlaces);
        }
        out.setDropoffAvailable(in.getDropoffAvailable());
        if (in.getDropoffAvailable()) {
            out.setCustomDropoffPlaceAllowed(in.getCustomDropoffPlaceAllowed());
        }
        if (in.getDropoffAvailable() && !in.getCustomDropoffPlaceAllowed()) {
            in.getDropoffPlaces().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addDropoffPlaces);
        }
        out.setProductCategory(restToGrpc(in.getProductCategory()));
        in.getTicketSupport().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addTicketSupport);
        in.getCountries().forEach(out::addCountries);
        in.getCities().forEach(out::addCities);
        if (in.getBookingType() == BookingType.DATE_AND_TIME) {
            in.getStartTimes().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addStartTimes);
        }
        if (!in.getTicketSupport().contains(TicketSupport.TICKETS_NOT_REQUIRED)) {
            out.setTicketType(restToGrpc(in.getTicketType()));
        }
        out.setMeetingType(restToGrpc(in.getMeetingType()));
        if (in.getEnforcedLeadPassengerFields() != null) {
            in.getEnforcedLeadPassengerFields().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addEnforcedLeadPassengerFields);
        }
        if (in.getEnforcedTravellerFields() != null) {
            in.getEnforcedTravellerFields().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addEnforcedTravellerFields);
        }
        if (in.getExtras() != null) {
            in.getExtras().stream()
                    .map(GrpcRestMapper::restToGrpc)
                    .forEach(out::addExtras);
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Date restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.DateYMD in) {
        return io.bokun.inventory.common.api.grpc.Date.newBuilder()
                .setYear(in.getYear())
                .setMonth(in.getMonth())
                .setDay(in.getDay())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Price restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Price in) {
        return io.bokun.inventory.common.api.grpc.Price.newBuilder()
                .setCurrency(in.getCurrency())
                .setAmount(in.getAmount())
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PricingCategoryWithPrice restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PricingCategoryWithPrice in) {
        return io.bokun.inventory.common.api.grpc.PricingCategoryWithPrice.newBuilder()
                .setPricingCategoryId(in.getPricingCategoryId())
                .setPrice(restToGrpc(in.getPrice()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PricePerPerson restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PricePerPerson in) {
        io.bokun.inventory.common.api.grpc.PricePerPerson.Builder out = io.bokun.inventory.common.api.grpc.PricePerPerson.newBuilder();
        in.getPricingCategoryWithPrice().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addPricingCategoryWithPrice);
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.PricePerBooking restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.PricePerBooking in) {
        return io.bokun.inventory.common.api.grpc.PricePerBooking.newBuilder()
                .setPrice(restToGrpc(in.getPrice()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.RateWithPrice restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.RateWithPrice in) {
        io.bokun.inventory.common.api.grpc.RateWithPrice.Builder out = io.bokun.inventory.common.api.grpc.RateWithPrice.newBuilder()
                .setRateId(in.getRateId());
        if (in.getPricePerPerson() != null) {
            out.setPricePerPerson(restToGrpc(in.getPricePerPerson()));
        } else {
            assert in.getPricePerBooking() != null;
            out.setPricePerBooking(restToGrpc(in.getPricePerBooking()));
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ProductAvailabilityWithRatesResponse restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ProductAvailabilityWithRatesResponse in) {
        io.bokun.inventory.common.api.grpc.ProductAvailabilityWithRatesResponse.Builder out =
                io.bokun.inventory.common.api.grpc.ProductAvailabilityWithRatesResponse.newBuilder()
                .setCapacity(in.getCapacity())
                .setDate(restToGrpc(in.getDate()));
        if (in.getTime() != null) {
            out.setTime(restToGrpc(in.getTime()));
        }
        if (in.getPickupTime() != null) {
            out.setPickupTime(restToGrpc(in.getPickupTime()));
        }
        in.getRates().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addRates);
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ReservationResponse restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ReservationResponse in) {
        io.bokun.inventory.common.api.grpc.ReservationResponse.Builder out = io.bokun.inventory.common.api.grpc.ReservationResponse.newBuilder();
        if (in.getSuccessfulReservation() != null) {
            out.setSuccessfulReservation(io.bokun.inventory.common.api.grpc.SuccessfulReservation.newBuilder()
                                                 .setReservationConfirmationCode(in.getSuccessfulReservation().getReservationConfirmationCode()));
        } else {
            out.setFailedReservation(io.bokun.inventory.common.api.grpc.FailedReservation.getDefaultInstance());
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.BinaryTicket restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.BinaryTicket in) {
        return io.bokun.inventory.common.api.grpc.BinaryTicket.newBuilder()
                .setTicketContent(ByteString.copyFrom(BASE64_DECODER.decode(in.getTicketContent())))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.QrTicket restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.QrTicket in) {
        io.bokun.inventory.common.api.grpc.QrTicket.Builder out = io.bokun.inventory.common.api.grpc.QrTicket.newBuilder()
                .setTicketBarcode(in.getTicketBarcode());
        if (in.getOfflineCode() != null) {
            out.setOfflineCode(in.getOfflineCode());
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.DataMatrixTicket restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.DataMatrixTicket in) {
        io.bokun.inventory.common.api.grpc.DataMatrixTicket.Builder out = io.bokun.inventory.common.api.grpc.DataMatrixTicket.newBuilder()
                .setTicketBarcode(in.getTicketBarcode());
        if (in.getOfflineCode() != null) {
            out.setOfflineCode(in.getOfflineCode());
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.Ticket restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.Ticket in) {
        io.bokun.inventory.common.api.grpc.Ticket.Builder out = io.bokun.inventory.common.api.grpc.Ticket.newBuilder();
        if (in.getBinaryTicket() != null) {
            out.setBinaryTicket(restToGrpc(in.getBinaryTicket()));
        } else if (in.getQrTicket() != null) {
            out.setQrTicket(restToGrpc(in.getQrTicket()));
        } else if (in.getDataMatrixTicket() != null) {
            out.setDataMatrixTicket(restToGrpc(in.getDataMatrixTicket()));
        } else {
            throw new IllegalStateException("Unsupported or unset ticket type");
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.TicketPerPricingCategory restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.TicketPerPricingCategory in) {
        return io.bokun.inventory.common.api.grpc.TicketPerPricingCategory.newBuilder()
                .setPricingCategory(in.getPricingCategory())
                .setTicket(restToGrpc(in.getTicket()))
                .build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.TicketsPerPricingCategory restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.TicketsPerPricingCategory in) {
        io.bokun.inventory.common.api.grpc.TicketsPerPricingCategory.Builder out = io.bokun.inventory.common.api.grpc.TicketsPerPricingCategory.newBuilder();
        in.getTicketPerPricingCategory().stream()
                .map(GrpcRestMapper::restToGrpc)
                .forEach(out::addTickets);
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.SuccessfulBooking restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.SuccessfulBooking in) {
        io.bokun.inventory.common.api.grpc.SuccessfulBooking.Builder out = io.bokun.inventory.common.api.grpc.SuccessfulBooking.newBuilder()
                .setBookingConfirmationCode(in.getBookingConfirmationCode());
        if (in.getTicketsPerPassenger() != null) {
            out.setTicketsPerPassenger(restToGrpc(in.getTicketsPerPassenger()));
        } else {
            assert in.getBookingTicket() != null;
            out.setBookingTicket(restToGrpc(in.getBookingTicket()));
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.FailedBooking restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.FailedBooking in) {
        io.bokun.inventory.common.api.grpc.FailedBooking.Builder out = io.bokun.inventory.common.api.grpc.FailedBooking.newBuilder();
        if (in.getBookingError() != null) {
            out.setBookingError(in.getBookingError());
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.ConfirmBookingResponse restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.ConfirmBookingResponse in) {
        io.bokun.inventory.common.api.grpc.ConfirmBookingResponse.Builder out = io.bokun.inventory.common.api.grpc.ConfirmBookingResponse.newBuilder();
        if (in.getSuccessfulBooking() != null) {
            out.setSuccessfulBooking(restToGrpc(in.getSuccessfulBooking()));
        } else {
            assert in.getFailedBooking() != null;
            out.setFailedBooking(restToGrpc(in.getFailedBooking()));
        }
        return out.build();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.SuccessfulCancellation restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.SuccessfulCancellation in) {
        return io.bokun.inventory.common.api.grpc.SuccessfulCancellation.getDefaultInstance();
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.FailedCancellation restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.FailedCancellation in) {
        if (in.getCancellationError() != null) {
            return io.bokun.inventory.common.api.grpc.FailedCancellation.newBuilder()
                    .setCancellationError(in.getCancellationError())
                    .build();
        } else {
            return io.bokun.inventory.common.api.grpc.FailedCancellation.getDefaultInstance();
        }
    }

    @Nonnull
    public static io.bokun.inventory.common.api.grpc.CancelBookingResponse restToGrpc(@Nonnull io.bokun.inventory.plugin.api.rest.CancelBookingResponse in) {
        if (in.getSuccessfulCancellation() != null) {
            return io.bokun.inventory.common.api.grpc.CancelBookingResponse.newBuilder()
                    .setSuccessfulCancellation(restToGrpc(in.getSuccessfulCancellation()))
                    .build();
        } else {
            assert in.getFailedCancellation() != null;
            return io.bokun.inventory.common.api.grpc.CancelBookingResponse.newBuilder()
                    .setFailedCancellation(restToGrpc(in.getFailedCancellation()))
                    .build();
        }
    }
}

### Inventory Service plugin harness.

This project should give you a head start if you are implementing Inventory Service plugin in Java.

Please [read the documentation](https://docs.bokun.io/docs/apps-and-the-bokun-app-store/developer-api-and-inventory-services/inventory-service-for-experiences) if you haven't done so.

`Main` class, if launched with appropriate parameters, will invoke all the APIs your plugin should implement as per contract.
Note that this harness is not extensive: there may be code paths which may not be covered by this, especially since this tool uses randomization a lot.
E.g. it uses randomly available product and its availability for making a reservation/booking.  

Requires Java 8.

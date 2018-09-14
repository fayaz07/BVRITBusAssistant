package fz.bvritbusassistant;

public class DataModel {

    private String latitude, longitude, presentLocation, lastSeen, phone, routeCode,nearbyPlaces;

    public DataModel() {
    }

    public DataModel(String latitude, String longitude, String presentLocation, String lastSeen, String phone, String routeCode, String nearbyPlaces) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.presentLocation = presentLocation;
        this.lastSeen = lastSeen;
        this.phone = phone;
        this.routeCode = routeCode;
        this.nearbyPlaces = nearbyPlaces;
    }

    public String getNearbyPlaces() {
        return nearbyPlaces;
    }

    public void setNearbyPlaces(String nearbyPlaces) {
        this.nearbyPlaces = nearbyPlaces;
    }

    public DataModel(String latitude, String longitude, String presentLocation, String lastSeen, String phone, String routeCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.presentLocation = presentLocation;
        this.lastSeen = lastSeen;
        this.phone = phone;
        this.routeCode = routeCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPresentLocation() {
        return presentLocation;
    }

    public void setPresentLocation(String presentLocation) {
        this.presentLocation = presentLocation;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
}

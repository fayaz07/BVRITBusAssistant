package fz.bvritbusassistant;

public class ClientDataModel {

    private String uid,phone,routeCode,index;

    public ClientDataModel() {
    }

    public ClientDataModel(String uid, String phone, String routeCode, String index) {
        this.uid = uid;
        this.phone = phone;
        this.routeCode = routeCode;
        this.index = index;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}

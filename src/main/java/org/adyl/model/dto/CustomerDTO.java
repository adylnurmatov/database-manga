package org.adyl.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

public class CustomerDTO implements DefaultDTO {
    private Integer id;
    @NotEmpty(message = "Specify the name!")
    private String name;
    @NotEmpty(message = "Specify address!")
    private String address;
    @NotEmpty(message = "Specify phone!")
    @Pattern(regexp = "^\\+996\\d{9}$", message = "Number must be in format +996xxx xx xx xx")
    private String phone;
    @NotEmpty(message = "Specify email!")
    @Pattern(regexp = "^.+@.+\\..+$", message = "Email must have next pattern: example@gmail.com")
    private String email;
    private List<Integer> orders;

    public CustomerDTO() {
        orders = new ArrayList<>();
    }

    public CustomerDTO(String name, String address, String phone, String email, List<Integer> orders) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.orders = orders;
    }

    public CustomerDTO(Integer id, String name, String address, String phone, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getOrders() {
        return orders;
    }

    public void setOrders(List<Integer> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", orders=" + orders +
                '}';
    }
}

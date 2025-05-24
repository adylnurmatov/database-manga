package org.adyl.security.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.adyl.model.dto.CustomerDTO;
import org.adyl.model.dto.DefaultDTO;

public class StoreRegistrationUserDTO implements DefaultDTO {
    private Integer id;

    @NotEmpty(message = "Specify the username!")
    private String username;

    @NotEmpty(message = "Specify password!")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$",
            message = "Minimum 8 characters in length, at least one uppercase and one lowercase English letter, " +
                    "at least one digit and at least one special character!")
    @Length(min = 8, message = "Minimal length is 8!")
    private String password;

    private CustomerDTO customer;
    private String image;

    public StoreRegistrationUserDTO() {
    }

    public StoreRegistrationUserDTO(String username, String password, CustomerDTO customer) {
        this.username = username;
        this.password = password;
        this.customer = customer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "BookStoreUserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", customer=" + customer +
                ", image='" + image + '\'' +
                '}';
    }
}

package org.adyl.security.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.adyl.model.dto.CostumerDTO;
import org.adyl.model.dto.DefaultDTO;

public class StoreUserDTO implements DefaultDTO {
    private Long id;

    @NotEmpty(message = "Specify the username!")
    private String username;

    @NotEmpty(message = "Specify password!")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$",
            message = "Minimum 8 characters in length, at least one uppercase and one lowercase English letter, " +
                    "at least one digit and at least one special character!")
    @Length(min = 8, message = "Minimal length is 8!")
    private String password;
    private String image;

    @NotEmpty
    private String roles;

    private CostumerDTO costumer;

    public StoreUserDTO() {
    }

    public StoreUserDTO(String username, String password, String roles, CostumerDTO costumer) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.costumer = costumer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public CostumerDTO getCostumer() {
        return costumer;
    }

    public void setCostumer(CostumerDTO costumer) {
        this.costumer = costumer;
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
                ", roles='" + roles + '\'' +
                ", costumer=" + costumer +
                ", image='" + image + '\'' +
                '}';
    }
}

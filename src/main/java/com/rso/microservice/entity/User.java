package com.rso.microservice.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @Column(name = "LAST_NAME", length = 100, nullable = false)
    private String lastName;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "PASSWORD", length = 100, nullable = false)
    private String password;

    @Column(name = "USERNAME", length = 100, nullable = false, unique = true)
    private String username;

    @ManyToOne(targetEntity = Role.class)
    private Role role;

    @OneToMany(targetEntity = UserFavoriteProduct.class, mappedBy = "user")
    private List<UserFavoriteProduct> userFavoriteProducts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<UserFavoriteProduct> getUserFavoriteProducts() {
        return userFavoriteProducts;
    }

    public void setUserFavoriteProducts(List<UserFavoriteProduct> userFavoriteProducts) {
        this.userFavoriteProducts = userFavoriteProducts;
    }
}

package com.example.oneidentity.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "user")
public class User implements java.io.Serializable
{
    private static final long serialVersionUID = 3908527171451767732L;

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("firstName")
    @Expose
    private String firstName;

    @SerializedName("lastName")
    @Expose
    private String lastname;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("picture")
    @Expose
    private String picture;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return Character.toUpperCase(title.charAt(0)) + title.substring(1);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return Character.toUpperCase(firstName.charAt(0)) + firstName.substring(1);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastname() {
        return Character.toUpperCase(lastname.charAt(0)) + lastname.substring(1);
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return uid == user.uid && Objects.equal(id, user.id) && Objects.equal(title, user.title) && Objects.equal(firstName, user.firstName) && Objects.equal(lastname, user.lastname) && Objects.equal(email, user.email) && Objects.equal(picture, user.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid, id, title, firstName, lastname, email, picture);
    }
}

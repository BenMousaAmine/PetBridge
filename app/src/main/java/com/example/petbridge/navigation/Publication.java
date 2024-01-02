package com.example.petbridge.navigation;

public class Publication {
    private String userId ;

    private String pubImage ;
    private String pubText ;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    private String nome ;
    private String profileImage ;


    public Publication (){
    }

    public Publication(String userId , String pubImage, String pubText) {
        this.userId = userId ;
        this.pubImage = pubImage;
        this.pubText = pubText;
    }






    public String getPubImage() {
        return pubImage;
    }

    public void setPubImage(String pubImage) {
        this.pubImage = pubImage;
    }



    public String getPubText() {
        return pubText;
    }

    public void setPubText(String pubText) {
        this.pubText = pubText;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

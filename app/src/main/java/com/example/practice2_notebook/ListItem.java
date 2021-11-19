package com.example.practice2_notebook;

public class ListItem {
    private String id;
    private String title;
    private String author;
    private String date;
    private String content;
    public ListItem(){

    }

    public ListItem(String id, String title, String author, String date, String content){
        this.id=id;
        this.title=title;
        this.author=author;
        this.date=date;
        this.content=content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

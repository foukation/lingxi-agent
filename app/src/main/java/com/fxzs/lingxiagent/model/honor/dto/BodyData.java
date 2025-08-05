package com.fxzs.lingxiagent.model.honor.dto;

import java.util.List;

public class BodyData {
    private String text;
    private Number cardType;
    private List<CardData> jsCards;
    private List<ButtonsData> buttons;
    private List<HtmlInfo> htmls;

    public BodyData(String text, Number cardType, List<CardData> jsCards,
                    List<ButtonsData> buttons, List<HtmlInfo> htmls) {
        this.text = text;
        this.cardType = cardType;
        this.jsCards = jsCards;
        this.buttons = buttons;
        this.htmls = htmls;
    }

    // Getters
    public String getText() { return text; }
    public Number getCardType() { return cardType; }
    public List<CardData> getJsCards() { return jsCards; }
    public List<ButtonsData> getButtons() { return buttons; }
    public List<HtmlInfo> getHtmls() { return htmls; }

    // Setters
    public void setText(String text) { this.text = text; }
    public void setCardType(Number cardType) { this.cardType = cardType; }
    public void setJsCards(List<CardData> jsCards) { this.jsCards = jsCards; }
    public void setButtons(List<ButtonsData> buttons) { this.buttons = buttons; }
    public void setHtmls(List<HtmlInfo> htmls) { this.htmls = htmls; }
}

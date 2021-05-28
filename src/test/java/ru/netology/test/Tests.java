package ru.netology.test;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.netology.data.CardsInfo;
import ru.netology.data.Login;
import ru.netology.pages.DashboardPage;
import ru.netology.pages.LoginPage;
import ru.netology.pages.TransferPage;
import ru.netology.pages.VerificationPage;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Tests {
    private DashboardPage dashboardPage;
    private final int amount = 500;

    @BeforeEach
    void setUp() {
        open("http://localhost:7777");
        LoginPage loginPage = new LoginPage();
        Login.AuthInfo authInfo = Login.getAuthInfo();
        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        dashboardPage = verificationPage.validCode();
        dashboardPage.equalizeBalance();
    }

    @AfterEach
    void theEnd() {
        dashboardPage.equalizeBalance();
        dashboardPage.theEnd();
    }

    @Test
    @DisplayName("Трансфер с первой на вторую карту")
    void shouldTransferFrom1to2() {
        int expected1 = dashboardPage.getCardBalance(0) - amount;
        int expected2 = dashboardPage.getCardBalance(1) + amount;
        TransferPage transferPage = dashboardPage.transferTo(1);
        transferPage.transaction(Integer.toString(amount), CardsInfo.cardNumber(0));
        int actual1 = dashboardPage.getCardBalance(0);
        int actual2 = dashboardPage.getCardBalance(1);
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    @Test
    @DisplayName("Трансфер со второй на первую карту")
    void shouldTransferFrom2to1() {
        int expected1 = dashboardPage.getCardBalance(1) - amount;
        int expected2 = dashboardPage.getCardBalance(0) + amount;
        TransferPage transferPage = dashboardPage.transferTo(0);
        transferPage.transaction(Integer.toString(amount), CardsInfo.cardNumber(1));
        int actual1 = dashboardPage.getCardBalance(1);
        int actual2 = dashboardPage.getCardBalance(0);
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    @Test
    @DisplayName("Овердрафт проходит - баг")
    void shouldNotTransferOverdraft() {
        int overdraft = 11000;
        TransferPage transferPage = dashboardPage.transferTo(0);
        transferPage.transaction(Integer.toString(overdraft), CardsInfo.cardNumber(1));
    }

    @Test
    @DisplayName("Нулевая сумма проходит - баг")
    void shouldNotTransferZero() {
        int zero = 0;
        TransferPage transferPage = dashboardPage.transferTo(0);
        transferPage.transaction(Integer.toString(zero), CardsInfo.cardNumber(1));
    }

    @Test
    @DisplayName("Минус нельзя отправить")
    void shouldNotTransferMinus() {
        int minus = -10000;
        TransferPage transferPage = dashboardPage.transferTo(0);
        transferPage.transaction(Integer.toString(minus), CardsInfo.cardNumber(1));
    }

    @Test
    @DisplayName("С несуществующих карт нельзя пополнять")
    void shouldShowErrorMessage() {
        int error = 100;
        TransferPage transferPage = dashboardPage.transferTo(0);
        transferPage.transaction(Integer.toString(error), CardsInfo.unavailableCardNumber(1));
        $(".notification__content").shouldHave(Condition.exactText("Ошибка! Произошла ошибка"));
        transferPage.transaction(Integer.toString(error), CardsInfo.unavailableCardNumber(0));
        $(".notification__content").shouldHave(Condition.exactText("Ошибка! Произошла ошибка"));
        transferPage.transaction(Integer.toString(error), CardsInfo.cardNumber(1));
    }
}


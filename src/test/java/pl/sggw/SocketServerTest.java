package pl.sggw;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.refresh;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SocketServerTest {
  static Properties prop;
  static String path = "";

  static {
    prop = new Properties();
    Configuration.timeout = 10 * 1000;
    try {
      prop.load(new FileInputStream("src/main/resources/application.properties"));
    } catch (IOException e) {
      e.printStackTrace();
      try {
        prop.load(new FileInputStream("src/test/resources/application.properties"));
      } catch (IOException f) {
        f.printStackTrace();
        prop.put("port", "8080");
        prop.put("host", "http://localhost");
      }
    }
    path = prop.get("host") + ":" + prop.get("port");
  }

  @BeforeEach
  void clearBooks() {
    open(path + "/manage.html");
    refresh();
    $("form").$("input[type=submit]").click();
  } //czyszczenie między testami

  @Test
  void manageTest() {  //sprawdzenie czy na stronie manage jest formularz z /clearBooksAction
    open(path + "/manage.html");
    refresh();
    Assertions.assertEquals($("form").getDomAttribute("action"), "/clearBooksAction");
    $("form").$("input[type=submit]").click();  //czyszczenie książek
  }

  @Test
  void booksEmptyTest() {  //sprawdzanie czy po wyczyszczeniu biblioteki jest tekst "Brak książek"
    clearBooks();
    open(path + "/books.html");
    refresh();
    Assertions.assertTrue($("div").getOwnText().equalsIgnoreCase("Brak książek"));
  }


  @Test
  void addBookTest() {
    clearBooks();
    open(path + "/addBook.html");
    refresh();
    Assertions.assertTrue($("form").getDomAttribute("method").equalsIgnoreCase("post"));  //sprawdzanie czy addBooks jest POSTem
    String title = "Tytuł 1";
    String authorName = "Imię 1";
    String authorSurname = "Nazwisko 1";
    $("form").$("input[name=\"title\"]").setValue(title);
    $("form").$("input[name=\"authorName\"]").setValue(authorName);
    $("form").$("input[name=\"authorSurname\"]").setValue(authorSurname);
    $("form").$("input[type=submit]").click();

    //sprawdzenie czy w tabeli pierwsza linia jest równa wysłym wartościom
    $$("tbody tr").shouldHave(size(1));
    $("tbody tr").$(".id").shouldBe(Condition.text("1"));
    $("tbody tr").$(".title").shouldBe(Condition.text(title));
    $("tbody tr").$(".authorName").shouldBe(Condition.text(authorName));
    $("tbody tr").$(".authorSurname").shouldBe(Condition.text(authorSurname));
  }

  @Test
  void addBooks2Test() {  //dodanie 5 książek pod rząd
    clearBooks();
    for (int i = 1; i <= 2; i++) {
      open(path + "/addBook.html");
      refresh();
      $("table").should(Condition.disappear);
      $("form").shouldBe(Condition.visible);
      String title = "Tytuł " + i;
      String authorName = "Imię "+i;
      String authorSurname = "Nazwisko "+i;
      $("form").$("input[name=\"title\"]").setValue(title);
      $("form").$("input[name=\"authorName\"]").setValue(authorName);
      $("form").$("input[name=\"authorSurname\"]").setValue(authorSurname);
      $("form").$("input[type=submit]").click();
    }
    open(path+"/books.html");
    refresh();

    $$("tbody tr").shouldBe(size(2));
  }

  @Test
  void updateBookTest() {
    clearBooks();
    open(path + "/addBook.html");
    refresh();
    Assertions.assertTrue($("form").getDomAttribute("method").equalsIgnoreCase("post"));  //sprawdzanie czy addBooks jest POSTem
    String title = "Tytuł 1";
    String authorName = "Imię 1";
    String authorSurname = "Nazwisko 1";
    $("form").$("input[name=\"title\"]").setValue(title);
    $("form").$("input[name=\"authorName\"]").setValue(authorName);
    $("form").$("input[name=\"authorSurname\"]").setValue(authorSurname);
    $("form").$("input[type=submit]").click();

    open(path + "/updateBook.html?id=1");
    refresh();
    Assertions.assertTrue(
        $("form").getDomAttribute("action").equalsIgnoreCase("/updateBookAction"));  //istnieje formularz do zmiany książki z indeksem 1

    $("form input[name=\"title\"]").setValue("Edycja");
    $("form input[name=\"authorName\"]").setValue("Edycja");
    $("form input[name=\"authorSurname\"]").setValue("Edycja");

    $("form input[type=submit]").click();

    open(path + "/books.html");

    $("tbody tr .title").shouldBe(Condition.text("Edycja"));
    $("tbody tr .authorName").shouldBe(Condition.text("Edycja"));
    $("tbody tr .authorSurname").shouldBe(Condition.text("Edycja"));
  }
}
//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
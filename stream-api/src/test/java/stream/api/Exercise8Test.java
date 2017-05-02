package stream.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import common.test.tool.annotation.Difficult;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.entity.Item;
import common.test.tool.entity.Shop;

public class Exercise8Test extends ClassicOnlineStore {

    @Difficult
    @Test
    public void itemsNotOnSale() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a set of item names that are in {@link Customer.wantToBuy} but not on sale in any shop.
         */
        List<String> itemListOnSale = shopStream.flatMap(shop -> shop.getItemList().stream().map(Item::getName))
                .collect(Collectors.toList());
        Set<String> itemSetNotOnSale = customerStream.flatMap(cus -> cus.getWantToBuy().stream()).map(Item::getName)
                .filter(name -> itemListOnSale.stream().noneMatch(name::equals))
                .collect(Collectors.toSet());

        assertThat(itemSetNotOnSale, hasSize(3));
        assertThat(itemSetNotOnSale, hasItems("bag", "pants", "coat"));
    }

    @Difficult
    @Test
    public void havingEnoughMoney() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a customer's name list including who are having enough money to buy all items they want which is on sale.
         * Items that are not on sale can be counted as 0 money cost.
         * If there is several same items with different prices, customer can choose the cheapest one.
         */
        List<Item> onSale = shopStream.flatMap(shop -> shop.getItemList().stream()).collect(Collectors.toList());
        Predicate<Customer> havingEnoughMoney = customer -> customer.getBudget() >= customer.getWantToBuy().stream()
                .mapToInt(
                        wantedItem -> onSale.stream()
                                .filter(shopItem -> shopItem.getName().equals(wantedItem.getName()))
                                .sorted((o1, o2) -> o1.getPrice() - o2.getPrice())
                                .findFirst().map(Item::getPrice).orElse(0))
                .sum();
        List<String> customerNameList = customerStream.filter(havingEnoughMoney).map(Customer::getName)
                .collect(Collectors.toList());

        assertThat(customerNameList, hasSize(7));
        assertThat(customerNameList, hasItems("Joe", "Patrick", "Chris", "Kathy", "Alice", "Andrew", "Amy"));
    }
}

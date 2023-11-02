package de.unistuttgart.t2.modulith.ui.web;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.OrderService;
import de.unistuttgart.t2.modulith.ui.domain.ItemToAdd;
import de.unistuttgart.t2.modulith.ui.domain.PaymentDetails;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * Defines the http endpoints of the UI.
 *
 * @author maumau
 */
@Controller
public class UIController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public UIController(@Autowired CartService cartService,
                        @Autowired InventoryService inventoryService,
                        @Autowired OrderService orderService) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    ////// PAGES TO REALLY LOOK AT ///////////

    @GetMapping("/ui/")
    public String index(Model model) {
        model.addAttribute("title", "T2-Project");
        return "index";
    }

    @GetMapping("/ui/products")
    public String products(Model model) {
        model.addAttribute("title", "Products");
        model.addAttribute("item", new ItemToAdd());

        List<Product> products = inventoryService.getAllProducts();

        model.addAttribute("productslist", products);

        return "category";
    }

    @GetMapping("/ui/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("title", "Cart");
        model.addAttribute("item", new ItemToAdd());

        LOG.info("SessionID : " + session.getId());

        List<Product> products = cartService.getProductsInCart(session.getId());

        model.addAttribute("OrderItems", products);

        return "cart";
    }

    @GetMapping("/ui/confirm")
    public String confirm(Model model, HttpSession session) {

        model.addAttribute("title", "Confirm");
        model.addAttribute("details", new PaymentDetails());

        return "order";
    }

    @GetMapping("/error")
    public String error(Model model, HttpSession session) {

        model.addAttribute("title", "Error");

        return "error_page";
    }

    ////////// ACTIONS /////////////

    @PostMapping("/ui/add")
    public String add(@ModelAttribute("item") ItemToAdd item, HttpSession session) {

        LOG.info("SessionID : " + session.getId());
        LOG.info("Item to Add : " + item.toString());

        cartService.addItemToCart(session.getId(), item.getProductId(), item.getUnits());

        return "product";
    }

    @PostMapping("/ui/delete")
    public RedirectView delete(@ModelAttribute("item") ItemToAdd item, RedirectAttributes redirectAttributes,
        HttpSession session) {

        LOG.info("SessionID : " + session.getId());
        LOG.info("Item to Delete : " + item.toString());

        cartService.deleteItemFromCart(session.getId(), item.getProductId(), -1 * item.getUnits());

        // TODO redirect : to display deleted products

        return new RedirectView("/ui/cart", true);
    }

    @PostMapping("/ui/confirm")
    public String confirm(@ModelAttribute("details") PaymentDetails details, Model model, HttpSession session) {
        LOG.info("SessionID : " + session.getId());

        orderService.confirmOrder(session.getId(), details.getCardNumber(), details.getCardOwner(), details.getChecksum());

        model.addAttribute("title", "Confirmed");

        // TODO : Display confirmation message :) / (or Failure)

        return "category";
    }
}
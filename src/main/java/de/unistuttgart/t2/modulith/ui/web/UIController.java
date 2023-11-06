package de.unistuttgart.t2.modulith.ui.web;

import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.ui.domain.ItemToAdd;
import de.unistuttgart.t2.modulith.ui.domain.PaymentDetails;
import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * Defines the http endpoints of the UI.
 *
 * @author maumau
 */
@Controller
@RequestMapping("/ui")
public class UIController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final UIBackendService uiBackendService;

    public UIController(@Autowired UIBackendService uiBackendService) {
        this.uiBackendService = uiBackendService;
    }

    ////// PAGES TO REALLY LOOK AT ///////////

    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("title", "T2-Project");
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Products");
        model.addAttribute("item", new ItemToAdd());

        List<Product> products = uiBackendService.getAllProducts();

        model.addAttribute("productslist", products);

        return "category";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("title", "Cart");
        model.addAttribute("item", new ItemToAdd());

        LOG.info("SessionID : " + session.getId());

        List<Product> products = uiBackendService.getProductsInCart(session.getId());

        model.addAttribute("OrderItems", products);

        return "cart";
    }

    @GetMapping("/confirm")
    public String confirm(Model model, HttpSession session) {

        model.addAttribute("title", "Confirm");
        model.addAttribute("details", new PaymentDetails());

        return "order";
    }

    ////////// ACTIONS /////////////

    @PostMapping("/add")
    public String add(@ModelAttribute("item") ItemToAdd item, Model model, HttpSession session) {

        LOG.info("SessionID : " + session.getId());
        LOG.info("Item to Add : " + item.toString());

        try {
            uiBackendService.addItemToCart(session.getId(), item.getProductId(), item.getUnits());
        } catch (ReservationFailedException e) {
            LOG.error("Failed to add item to cart : " + session.getId() + ".\nError message: " + e.getMessage());

            // TODO Display error message in UI
            model.addAttribute("title", "Error");
        }

        return "product";
    }

    @PostMapping("/delete")
    public RedirectView delete(@ModelAttribute("item") ItemToAdd item, RedirectAttributes redirectAttributes,
                               HttpSession session) {

        LOG.info("SessionID : " + session.getId());
        LOG.info("Item to Delete : " + item.toString());

        uiBackendService.deleteItemFromCart(session.getId(), item.getProductId(), item.getUnits());

        // TODO redirect : to display deleted products

        return new RedirectView("/ui/cart", true);
    }

    @PostMapping("/confirm")
    public String confirm(@ModelAttribute("details") PaymentDetails details, Model model, HttpSession session) {
        LOG.info("SessionID : " + session.getId());

        try {
            uiBackendService.confirmOrder(session.getId(), details.getCardNumber(), details.getCardOwner(), details.getChecksum());
            model.addAttribute("title", "Confirmed");
        } catch (OrderNotPlacedException e) {
            model.addAttribute("title", "Error");
        }

        // TODO : Display confirmation message :) / (or Failure)

        return "category";
    }


    ////////// UNDEFINED /////////////

    @RequestMapping("/**")
    public String error(Model model) {

        model.addAttribute("title", "Error");

        return "error_page";
    }
}

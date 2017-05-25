package com.shop.rest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.bean.ShopLocation;
import com.shop.service.ShoppingService;

@RestController
public class ShopController {
/**
 *
 * @param requestSD
 * @return
 * @throws IOException
 */
    @RequestMapping(value = "/shopping", consumes = "application/json", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody List<ShopLocation> address(@RequestBody String requestSD)
        throws IOException {

        ShoppingService shoppingService = new ShoppingService();
        List<ShopLocation> jsonInString = shoppingService.getShopLocation(requestSD);
        return jsonInString;
    }
/**
 *
 * @param customerData
 * @return
 * @throws IOException
 */
    @RequestMapping(value = "/customerShopping", consumes = "application/json", produces = "application/json", method = RequestMethod.POST)
    public @ResponseBody String customerAddress(@RequestBody String customerData)
        throws IOException {
        ShoppingService shoppingService = new ShoppingService();
        ObjectMapper objectMapper = new ObjectMapper();
        ShopLocation locationsReq = objectMapper.readValue(
            customerData.getBytes(Charset.forName("UTF-8")), ShopLocation.class);
        System.out.println("CusResp.."+locationsReq.getCustomerDetails().getCustomerAddress());
        String shortestDistance = shoppingService.getShortestDistance(locationsReq);
        System.out.println("ShortestDistance "+ shortestDistance);
        return shortestDistance;
    }
}

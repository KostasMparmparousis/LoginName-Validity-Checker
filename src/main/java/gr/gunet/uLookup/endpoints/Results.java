/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package gr.gunet.uLookup.endpoints;

import java.util.HashMap;

/**
 *
 * @author barbarousisk
 */
public interface Results {
    HashMap<String,String> getResults(boolean fromWeb);
    static HashMap<String,String> sendResponse(String code, String content, String title){
        HashMap<String,String> response=new HashMap<>();
        response.put("code", code);
        response.put("content", content);
        response.put("title", title);
        return response;
    }
}

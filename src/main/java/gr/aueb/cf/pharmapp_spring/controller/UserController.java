package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.dto.UserInsertDTO;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pharmapp")
@RequiredArgsConstructor
public class UserController {

    private final Logger LOGGER =
            LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final Mapper mapper;

    @GetMapping("/users/register")
    public String getUserForm (Model model){
        model.addAttribute("userInsertDTO", new UserInsertDTO());
        return "user-form";
    }

    public String insertUser(@Valid @ModelAttribute("userInsertDTO") UserInsertDTO userInsertDTO,
                             BindingResult bindingResult,
                             Model model, RedirectAttributes attrs){
        if(bindingResult.hasErrors()){
            return "user-form";
        }


    }


}

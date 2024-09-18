package kha.productsdemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kha.productsdemo.dto.request.ChangePasswordRequest;
import kha.productsdemo.dto.request.UpdateProductRequest;
import kha.productsdemo.dto.request.UpdateUserRequest;
import kha.productsdemo.dto.response.ShowUserAccount;
import kha.productsdemo.entity.User;
import kha.productsdemo.service.UserService;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String showAccountPage(Authentication authentication, Model model) {

            if (authentication != null && authentication.isAuthenticated()) {
                User user = userService.convertFromAuthenticationToUser(authentication);
                ShowUserAccount showUserAccount = userService.convertToShowAccount(user);
                model.addAttribute("user", showUserAccount);
                return "account";
            }

            return "redirect:/login";
    }

    @GetMapping("/editProfile")
    public String showEditProfilePage(Authentication authentication, Model model){
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.convertFromAuthenticationToUser(authentication);
            UpdateUserRequest updateUserRequest = userService.convertFromUserToUpdateProductRequest(user);
            model.addAttribute("updateUserRequest", updateUserRequest);
            return "editProfile";
        }
        else {
            return "redirect:/login";
        }
    }

    @PostMapping("/editProfile")
    public String editProfile(@Valid
                                  @ModelAttribute("updateUserRequest")UpdateUserRequest updateUserRequest,
                              BindingResult result,
                              Authentication authentication){
        if (result.hasErrors()){
            return "editProfile";
        }
        User user = userService.convertFromAuthenticationToUser(authentication);
        User updatedUser = userService.updateUser(updateUserRequest, user);
        userService.updateAuthentication(updatedUser);

        return "redirect:/user/account";
    }

    @GetMapping("/changePassword")
    public String showChangePasswordPage(Model model){
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        model.addAttribute("changePasswordRequest", changePasswordRequest);
        return "changePasswordPage";
    }
    @PostMapping("/changePassword")
    public String changePassword(Authentication authentication, @Valid @ModelAttribute("changePasswordRequest")
                                     ChangePasswordRequest changePasswordRequest,
                                  BindingResult result){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User currentUser = userService.convertFromAuthenticationToUser(authentication);
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())){
            FieldError error =
                    new FieldError("changePasswordRequest",
                            "currentPassword", "Current Password is wrong");
            result.addError(error);
        }
        if (result.hasErrors()){
            changePasswordRequest.setConfirmPassword("");
            changePasswordRequest.setCurrentPassword("");
            changePasswordRequest.setNewPassword("");
            return "changePasswordPage";
        }
        userService.changePassword(currentUser, changePasswordRequest);
        return "redirect:/user/account";
    }
}

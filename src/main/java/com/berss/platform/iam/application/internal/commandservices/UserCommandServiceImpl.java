package com.berss.platform.iam.application.internal.commandservices;

import com.berss.platform.business.domain.model.aggregates.Manager;
import com.berss.platform.business.domain.model.entities.Company;
import com.berss.platform.business.domain.model.valueobjects.CompanyStatus;
import com.berss.platform.business.infrastructure.persistence.jpa.repositories.CompanyQueryRepository;
import com.berss.platform.business.infrastructure.persistence.jpa.repositories.ManagerRepository;
import com.berss.platform.iam.application.internal.outboundservices.token.TokenService;
import com.berss.platform.iam.domain.model.aggregates.User;
import com.berss.platform.iam.domain.model.commands.CreateUserCommand;
import com.berss.platform.iam.domain.model.commands.RegisterUserWithManagerCommand;
import com.berss.platform.iam.domain.model.commands.SignInCommand;
import com.berss.platform.iam.domain.model.dto.AuthenticatedUser;
import com.berss.platform.iam.domain.services.UserCommandService;
import com.berss.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.berss.platform.shared.domain.model.valueobjects.CompanyId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final CompanyQueryRepository companyQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  ManagerRepository managerRepository,
                                  CompanyQueryRepository companyQueryRepository,
                                  PasswordEncoder passwordEncoder,
                                  TokenService tokenService) {
        this.userRepository = userRepository;
        this.managerRepository = managerRepository;
        this.companyQueryRepository = companyQueryRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public User handle(CreateUserCommand command) {
        var manager = managerRepository.findById(command.managerId())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        var companyId = new CompanyId(manager.getCompany().getId());
        var hashedPassword = passwordEncoder.encode(command.rawPassword());

        User user = new User(command.username(), hashedPassword, command.managerId(), companyId.companyId());
        return userRepository.save(user);
    }

    @Override
    public Optional<AuthenticatedUser> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty())
            return Optional.empty();
        if (!passwordEncoder.matches(command.password(), user.get().getPassword()))
            return Optional.empty();

        var token = tokenService.generateToken(user.get().getUsername(), user.get().getCompanyId().companyId());
        return Optional.of(new AuthenticatedUser(user.get(), token));
    }

    @Override
    public User handle(RegisterUserWithManagerCommand command) {
        var existingCompany = companyQueryRepository.findByName(command.companyName());

        Company company;
        if (existingCompany.isPresent()) {
            company = existingCompany.get();
        } else {
            company = new Company(
                    command.companyName(),
                    command.companyDescription(),
                    command.companyEmail(),
                    CompanyStatus.ACTIVE
            );
        }

        var existingManager = managerRepository
                .findByCompanyNameAndCompanyEmail(command.companyName(), command.companyEmail());

        Manager manager;
        if (existingManager.isPresent()) {
            manager = existingManager.get();
        } else {
            try {
                manager = new Manager(command.managerFirstname(), command.managerLastname(), company);
                manager = managerRepository.save(manager);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new IllegalArgumentException("There's already a manager for this company.");
            }
        }

        var hashedPassword = passwordEncoder.encode(command.password());
        var user = new User(command.username(), hashedPassword, manager.getId(), manager.getCompany().getId());
        return userRepository.save(user);
    }
}

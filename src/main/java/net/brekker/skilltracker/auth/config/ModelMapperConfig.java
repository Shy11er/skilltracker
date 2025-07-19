package net.brekker.skilltracker.auth.config;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import net.brekker.skilltracker.auth.db.domain.Role;
import net.brekker.skilltracker.auth.db.domain.User;
import net.brekker.skilltracker.auth.dto.UserDto;
import net.brekker.skilltracker.common.enums.RoleName;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setFieldMatchingEnabled(true);
        modelMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);

        modelMapper.createTypeMap(User.class, UserDto.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> Optional.ofNullable(ctx.getSource())
                                    .map(source -> ((Set<Role>) source).stream()
                                            .map(role -> RoleName.valueOf(role.getName().name()))
                                            .toList())
                                    .orElse(Collections.emptyList()))
                            .map(User::getRoles, UserDto::setRoles);
                });

        return modelMapper;
    }
}

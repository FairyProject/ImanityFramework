package org.imanity.framework;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Service(name = "repository")
@Getter
public class RepositoryService {

    @Autowired
    private MongoService mongoService;

    private List<Repository> repositories;

    @PreInitialize
    public void preInit() {
        this.repositories = new ArrayList<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { Repository.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                Repository repository = (Repository) super.newInstance(type);

                repositories.add(repository);
                return repository;
            }
        });
    }

    @PostInitialize
    public void init() {
        for (Repository repository : this.repositories) {
            repository.init(this);
        }

        repositories.clear();
        repositories = null;
    }

}

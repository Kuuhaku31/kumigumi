FROM php

RUN docker-php-ext-install pdo_mysql
WORKDIR /var/www/html
CMD [ "php", "-S", "0.0.0.0:8000", "-t", "src" ]

EXPOSE 8000

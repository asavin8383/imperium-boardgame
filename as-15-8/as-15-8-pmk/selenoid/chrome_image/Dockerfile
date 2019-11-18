FROM selenoid/vnc_chrome:75.0

USER root

COPY enable_clock/ /home/selenium/.fluxbox/

USER selenium

COPY --chown=selenium:nogroup etalon_chrome/ /home/selenium/chrome/
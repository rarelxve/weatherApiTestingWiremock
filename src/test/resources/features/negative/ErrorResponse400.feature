@allure.label.owner:dkarayanov

Feature: Тестирование получения ошибки 400 (без указания q/ошибка 1003)

  @smoke
  Scenario: Проверка соответствия ответа структуре из файла
    Given API эмулирует ответ с ошибкой 1003 из файла
    When Я отправляю GET-запрос на weather API без параметра q
    Then Ответ соответствует структуре из файла weather_response_q_is_missing
    And Ответ точно соответствует JSON из файла weather_response_q_is_missing
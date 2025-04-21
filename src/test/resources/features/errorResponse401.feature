Feature: Тестирование Weather API ошибка 401 (1002)

  Scenario: Проверка соответствия ответа структуре из файла
    Given API эмулирует ответ с ошибкой 1002 из файла
    When Я отправляю GET-запрос на weather API
    Then Ответ соответствует структуре из файла weather_error
    And Ответ точно соответствует JSON из файла weather_error

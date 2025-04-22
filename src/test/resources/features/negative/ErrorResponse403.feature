Feature: Тестирование Weather API ошибка 403 (2008)

  Scenario: Проверка соответствия ответа структуре из файла
    Given API эмулирует ответ с ошибкой 2008 из файла
    When Я отправляю GET-запрос на weather API c некорректным ключом
    Then Ответ соответствует структуре из файла weather_error_response_code_403.json
    And Ответ точно соответствует JSON из файла weather_error_response_code_403.json

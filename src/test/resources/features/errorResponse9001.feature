Feature: Тестирование Weather API ошибка 9001

  Scenario: Проверка соответствия ответа структуре из файла
    Given API эмулирует ответ с ошибкой 9001 из файла
    When Я отправляю POST-запрос с 51 городом, превышая лимит API
    Then Ответ соответствует структуре из файла weather_error_response_code_9001.json
    And Ответ точно соответствует JSON из файла weather_error_response_code_9001.json

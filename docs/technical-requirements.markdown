Технические требования
===================================================================================

Программа реализует систему документоборота, для методических материалов.
Клиент программы позволяет добавлять, редактировать, комментировать, оперелять
уровни доступа и получаться различные версии документа.
Сервер хранит информацию о пользователях, весии документов, комментарии и т.п.

### Функцинальные требования ###
#### Версионное храниение документов
Можно получить предыдущие версии документа и метаинформацию о них
- можно получить в виде списка
- хранится на сервере

#### Категоризация документов
Докуметны разделены по специализации и типу документа.
- хранятся на сервере

#### Поддержка различных прав на создание, доступ и комментирование документов
Существуют группы пользователей, благодаря которым можно гибко управлять правами
пользователей.
- неактивные/невидимые кнопки и остутствие данных, к которым нет доступа на чтение
- хранится на сервере

#### Поддержка комментирования
Поддержка комментариев к отдельным версиям документа
- список/дерево комментариев
- хранится на сервере

#### Поддержка различных типов документов
По-умолчанию использование бинарных форматов, но существует возможность поддержки
текстовые или смешанных/пользовательских форматов, при помощи создания плагинов



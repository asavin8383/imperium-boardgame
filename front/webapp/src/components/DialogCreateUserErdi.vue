<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog v-model="show" lazy persistent width="45%">
        <v-card class="record-card pa-3">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись' : 'Редактировать запись'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">
                <template v-if="originType === 'ppp' && action === 'edit'">
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Идентификатор ПАСД
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppa_ps_id }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Наименование ПАСД
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppa_ps_nm }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Имя хоста ПАСД
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppa_hostname_ps }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Доменное имя ПАСД
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_changedtm }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Описание сервиса
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_servicenm }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Сетевые адреса
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_addresses }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Статический сетевой адрес
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_stat_address }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Логины и пароли
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_logins }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Дата и время изменения записи
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_intdtm }}
                        </v-flex>
                    </v-layout>
                    <v-layout row class="pa-0 mb-2">
                        <v-flex md4 class="name-font">
                            Идентификатор АС 15.8
                        </v-flex>
                        <v-flex md8 class="value-font">
                            {{ recordInternal.ppp_servicenm }}
                        </v-flex>
                    </v-layout>
                    <v-divider class="mb-3"></v-divider>
                </template>
                <v-layout row :class="['pa-0 mb-2', originType === 'ppp' ? 'mt-2' : '']">
                    <v-flex md4 class="name-font">
                        <span>Тип ПАСД</span>
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.pasd_type"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Наименование конфигурации
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.config_nm"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Браузер
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.browser"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Платформа
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.id_what"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        proxy-dns-name
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.dnsname"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        proxy-port
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.proxyport"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        stub-url
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="recordInternal.url"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Источник данных
                    </v-flex>
                    <v-flex md8 class="value-font">
                        {{ recordInternal.origin }}
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Дата и время изменения записи АС 15.8
                    </v-flex>
                    <v-flex md8 class="value-font">
                        {{ recordInternal.change_dtm }}
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4 class="name-font">
                        Статус
                    </v-flex>
                    <div :style="recordInternal.status ? 'color: green' : 'color: red'">
                        {{ recordInternal.status ? 'Работает' : 'Не работает'}}
                    </div>
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-layout column class="pt-3">
                    <v-layout row align-center justify-center>
                        <v-btn color="primary" @click="checkRecord">Проверить</v-btn>
                    </v-layout>
                    <v-layout row>
                        <v-spacer></v-spacer>
                        <v-btn color="" flat @click="closeCard">Отмена</v-btn>
                        <v-btn color="primary" @click="save" :disabled="!validForm">Сохранить</v-btn>
                    </v-layout>
                </v-layout>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        name: "DialogCreateUserErdi",

        props: ['value', "action", "record"],
        /*        props: {
                    value: '',
                    id: null,
                    type: null
                },*/

        data() {
            return {
                recordInternal: {
                    "id": 1,
                    "name": "Тестер",
                    "hostname": "test.ru",
                    "domainNames": "276-fz@rkn.gov.ru",
                    "serviceDescription": "d",
                    "networkAddresses": "d",
                    "origId": 2,
                    "ppnDt": "2019-10-19T14:27:44.461",
                    "effDt": "3000-01-01T00:00:00",
                    "cdate": "2019-09-02T12:03:36"
                }
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            show: {
                set: function (v) {
                    this.$emit('input', v)
                },
                get: function () {
                    return this.value;
                }
            },

            validForm() {

            }
        },

        watch: {
            record(v) {
                this.recordInternal = v;
            }
        },

        mounted() {
        },

        methods: {
            closeCard() {
                this.show = false;
            },

            save() {
                console.log("save");
                this.closeCard();
            }
        }
    }
</script>

<style>
    .value-font .v-input .v-textarea {
        display: none;
    }
    .value-font .v-input.v-textarea {
        padding-top: 0;
    }

    .value-font .v-input.v-input--checkbox {
        margin: 0;
        padding: 0;
    }
    .value-font .v-input.v-input--checkbox .v-messages{
        display: none;
    }
</style>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
</style>
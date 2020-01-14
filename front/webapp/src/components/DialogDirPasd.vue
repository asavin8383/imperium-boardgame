<template>
    <v-dialog v-model="isActive" lazy persistent width="45%">
        <v-card class="record-card pa-3">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись ПАСД' : 'Редактировать запись ПАСД'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">

                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>Идентификатор ПАСД</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.origId" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Наименование ПАСД</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.name" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Hostname ПАСД</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.hostname" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Доменное имя</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.domainNames" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Логины и пароли</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.credentials" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">ipAccessFgis</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.ipAccessFgis" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Сетевые адреса</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.networkAddresses" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Описание сервиса</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.serviceDescription" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>

                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Источник данных</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.source" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>

                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Дата и время последнего изменения</v-flex>
                    <v-flex md7 class="name-font">
                        <v-text-field v-model="recordInternal.cDate" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>

            </v-card-text>
            <v-card-actions>
                <v-layout column class="pt-3">
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
    import _ from 'lodash';
    import Toggleable from "vuetify/lib/mixins/toggleable";
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'

    export default {
        name: "DialogDirPasd",
        mixins: [Toggleable],
        props: ['value', "action", "record"],

        data() {
            return {
                loading: false,
                recordInternal: {
                    name: "-",
                    hostname: "-",
                    credentials: "-",
                    domainNames: "-",
                    ipAccessFgis: "-",
                    networkAddresses: "-",
                    serviceDescription: "-",
                }
            }
        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showWarnMsg: {
                type: VueNotifications.types.warn,
                title: '',
                message: '',
            },
        },

        computed: {
            validForm() {
                return this.recordInternal.name && this.recordInternal.hostname
            }
        },

        watch: {
            isActive(v) {
                if (v) {
                    if (this.action === 'create') {
                        this.recordInternal.origId = null;
                        this.recordInternal.name = '';
                        this.recordInternal.hostname = '';
                        this.recordInternal.credentials = '';
                        this.recordInternal.domainNames = '';
                        this.recordInternal.ipAccessFgis = '';
                        this.recordInternal.networkAddresses = '';
                        this.recordInternal.serviceDescription = '';
                    }
                    else if (this.action ==='edit') {
                        this.recordInternal.origId = this.record.origId;
                        this.recordInternal.name = this.record.name;
                        this.recordInternal.hostname = this.record.hostname;
                        this.recordInternal.credentials = this.record.credentials;
                        this.recordInternal.domainNames = this.record.domainNames;
                        this.recordInternal.ipAccessFgis = this.record.ipAccessFgis;
                        this.recordInternal.networkAddresses = this.record.networkAddresses;
                        this.recordInternal.serviceDescription = this.record.serviceDescription;
                    }
                }
            }
        },

        methods: {
            closeCard() {
                this.isActive = false;
            },

            save() {
                const data = {
                    Id: this.recordInternal.origId,
                    Name: this.recordInternal.name,
                    Hostname: this.recordInternal.hostname,
                    Credentials: this.recordInternal.credentials,
                    DomainNames: this.recordInternal.domainNames,
                    IpAccessFgis: this.recordInternal.ipAccessFgis,
                    NetworkAddresses: this.recordInternal.networkAddresses,
                    ServiceDescription: this.recordInternal.serviceDescription,
                };
                this.loading = true;
                this.$axios.post(this.$urls.PASD_INFO, data).then(resp => {
                    const message = (this.action === 'create') ? "Запись успешно создана" : "Запись успешно сохранена";
                    this.showSuccessMsg({message});
                    this.closeCard();
                }).catch(e => {
                    console.log('error ', e);
                    if(CheckStatus.checkStatus403(e, this.showWarnMsg))return;
                    if (e.data){
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: e.data.message});
                        else
                            this.showErrorMsg({message: e.data.message});
                    }
                }).finally(() => {
                    this.loading = false;
                })
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
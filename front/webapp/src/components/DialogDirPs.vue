<template>
    <v-dialog v-model="isActive" lazy persistent width="45%">
        <v-card class="record-card pa-4">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись ПС' : 'Редактировать запись ПС'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">

                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>ID</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.origId" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>

                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Наименование ОПС</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.name" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Указатель страницы сайта (hostname)</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.hostname" hide-details :loading="loading"></v-text-field>
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
        name: "DialogDirPs",
        mixins: [Toggleable],
        props: ['value', "action", "record"],

        data() {
            return {
                loading: false,
                recordInternal: {
                    name: "-",
                    hostname: "-",
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
                    }
                    else if (this.action === 'edit') {
                        this.recordInternal.origId = this.record.origId;
                        this.recordInternal.name = this.record.name;
                        this.recordInternal.hostname = this.record.hostname;
                    }
                }
            },

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
                };

                this.loading = true;
                this.$axios.post(this.$urls.PS_INFO, data).then(resp => {
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

    .value-font .v-text-field__details {
        display: none;
    }
</style>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54);
    }
</style>
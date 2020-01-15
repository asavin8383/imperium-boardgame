<template>
    <v-dialog v-model="isActive" lazy persistent width="45%">
        <v-card class="record-card pa-4">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись' : 'Редактировать запись'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">

                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>Идентификатор</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.id" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>

                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Наименование</v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.name" @blur="validateNameOnBlur(recordInternal, $event)" @input="validateName(recordInternal, $event)" hide-details :loading="loading"></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>ID</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field v-model="recordInternal.origId" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>


                <v-layout align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>Тип</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-select v-model="recordInternal.accessTool" :items="accessToolTypes" hide-details></v-select>
                    </v-flex>
                </v-layout>

                <!--==========================================================================================-->
                <v-layout v-for="prop in propsByType" :key="prop.key" align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>{{prop.name}}</span></v-flex>
                    <v-flex md7 class="value-font" v-if="prop.name === 'Браузер' || prop.name === 'Платформа'">
                        <v-select v-model="prop.value" :items="prop.items" hide-details></v-select>
                    </v-flex>
                    <v-flex md7 class="value-font" v-else>
                        <v-text-field v-model="prop.value" hide-details></v-text-field>
                    </v-flex>
                </v-layout>
                <!--==========================================================================================-->


                <v-layout align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>Источник данных</span></v-flex>
                    <v-flex md7 class="value-font">
                        <v-text-field :value="recordInternal.origId | sourceType" hide-details disabled></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout v-if="action !== 'create'" align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Дата и время последнего изменения</v-flex>
                    <v-text-field :value="recordInternal.modificationDate | dateTableFormat" hide-details disabled></v-text-field>
                </v-layout>
                <v-layout align-end row fill-height class="pa-0 mb-2 mt-2">
                    <v-flex md5 class="name-font"><span>Статус</span></v-flex>
                    <v-switch hide-details v-model="robotStatusModel" :label="recordInternal.status | robotStatus"></v-switch>
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
    import moment from 'moment';
    import Toggleable from "vuetify/lib/mixins/toggleable";
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'

    export default {
        name: "dialog-robot-ps",
        mixins: [Toggleable],
        props: ['value', "action", "id"],

        data() {
            return {
                loading: false,
                recordInternal: {
                    name: "-",
                    hostname: "-",
                    status: "-",
                    accessTool: '',
                    robotProperties: {},
                },

                accessToolTypes: [ 'SEARCH_SYSTEM', 'VPN','PROXY', 'CAMELEO_XYZ', 'HIDEMYASS','HOLA' ],

                properties: {
                    common: [
                        {name: 'Браузер', key: 'BROWSER'},
                        {name: 'Платформа', key: 'PLATFORM'},
                        {name: 'Версия', key: 'VERSION'},
                    ],
                    search_system: [
                        {name: 'URL', key: 'SEARCH_SYSTEM_URL'},
                        {name: 'Задержка', key: 'INPUT_DELAY'},
                        {name: 'Глубина поиска', key: 'SEARCH_RESULT_LIMIT'},
                        {name: 'Тип вывода результатов', key: 'SEARCH_SYSTEM_RESULT_PAGE_TYPE'},
                        {name: 'Строка капчи', key: 'SEARCH_SYSTEM_XPATH_CAPTCHA'},
                        {name: 'Строка поля ввода', key: 'SEARCH_SYSTEM_XPATH_INPUT_FIELD'},
                        {name: 'Строка ссылок', key: 'SEARCH_SYSTEM_XPATH_ITEM_LINK'},
                        {name: 'Строка следующей страницы', key: 'SEARCH_SYSTEM_XPATH_NEXT_PAGE'},
                        {name: 'Прокси сервер', key: 'SEARCH_SYSTEM_PROXY'},
                        {name: 'Регулярное выражение "ссылок не найдено"', key: 'RESULT_NOT_FOUND_REGEXP'}
                    ],
                    robot: [
                        {name: 'STUB', key: 'STUB_URL'},
                        {name: 'Тип proxy', key: 'PROXY_TYPE'},
                        {name: 'DNS name proxy', key: 'PROXY_DNS_NAME'},
                        {name: 'Порт proxy', key: 'PROXY_PORT'},
                        {name: 'IGNORE_CAPTCHA_APPS', key: 'IGNORE_CAPTCHA_APPS'},
                    ],
                    extension: [
                        {name: 'ID расширения', key: 'EXTENSION_ID'},
                        {name: 'Версия расширения', key: 'EXTENSION_VERSION'},
                        {name: 'extension popup', key: 'EXTENSION_POPUP'},
                    ],

                },

            }
        },

        computed: {
            validForm() {
                return this.recordInternal.name && this.recordInternal.hostname
            },

            propsIdx() {
                return _(this.recordInternal.robotProperties).keyBy('key').mapValues("value").value();
            },

            propsByType() {
                const type = this.recordInternal.accessTool;
                const props =  type === 'SEARCH_SYSTEM' ? _.concat(this.properties.common, this.properties.search_system) :
                               type === 'VPN'           ? _.concat(this.properties.common, this.properties.robot) :
                               type === 'PROXY'         ? _.concat(this.properties.common, this.properties.robot) :
                               type === 'CAMELEO_XYZ'   ? _.concat(this.properties.common, this.properties.robot) :
                               type === 'HIDEMYASS'     ? _.concat(this.properties.common, this.properties.robot) :
                               type === 'HOLA'          ? _.concat(this.properties.common, this.properties.robot, this.properties.extension) :
                               [];
                props.forEach(p => {
                    p.value = this.propsIdx[p.key];
                    if(p.key === 'BROWSER'){
                        p.items = ['chrome'];
                    }
                    if(p.key === 'PLATFORM'){
                        p.items = ['ANY'];
                        p.value = this.propsIdx[p.key];
                        if(p.value)
                            p.value = p.value.toUpperCase();
                    }
                });
                return props;
            },
            robotType() {
                const type = this.recordInternal.accessTool;
                return type === 'SEARCH_SYSTEM' ? 'PS' : 'PASD';
            },

            robotStatusModel: {
                get() { return this.recordInternal.status === 'WORK' },
                set(s) {this.recordInternal.status = s ? 'WORK' : 'OUT_OF_WORK'}

            }
        },


        filters: {
            noData(v) {
                return (v == null || v === '') ? '-' : v;
            },

            sourceType(v) {
                return v === null ? null :
                v < 0 ? 'АС 15.8' :
                v > 0 ? 'ППП РА' :
                null
            },

            robotStatus(v) {
                return v === null ? null :
                v === 'WORK' ? 'работает' :
                v === 'OUT_OF_WORK' ? 'не работает' :
                null
            },

            robotType(v) {
                return v === null ? null :
                v === 'SEARCH_SYSTEM' ? 'ПС' :
                v === 'PASD' ? 'ПАСД' :
                null
            },

            dateTableFormat(date) {
                return date != null ? moment(date).locale('ru').format('DD.MM.YY hh:mm') :
                date;
            },

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


        watch: {
            isActive(v) {
                if (v) {
                    if (this.action === 'create') {
                        this.recordInternal.origId = null;
                        this.recordInternal.name = '';
                        this.recordInternal.hostname = '';
                    }
                    else if (this.action === 'edit') {
                        this.loadData()
                    }
                }
            },
        },

        methods: {

            closeCard() {
                this.isActive = false;
            },

            loadData() {
                this.$axios.get(`${this.$urls.ADMIN_ROBOT}/${this.id}`)
                .then(resp => {
                    _.extend(this.recordInternal, resp.data);
                })
            },

            save() {

                const params = {id: this.id};
                const data = this.recordInternal;

                data.robotProperties = _(this.propsByType).filter('value').map(p => ({'key': p.key, 'value': p.value})).value();
                data.type = this.robotType;

                this.loading = true;
                this.$axios.put(this.$urls.ADMIN_ROBOT, data, {params}).then(resp => {
                    const message = (this.action === 'create') ? "Запись успешно создана" : "Запись успешно сохранена";
                    this.showSuccessMsg({message});
                    this.closeCard();
                    this.$emit('changed');
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
            },

            async validateName(elem, value) {
                await this.$nextTick();
                let regexp = /[^a-zA-Z-]/g;
                this.recordInternal.name = value.replace(regexp, "").toLowerCase();
            },

            validateNameOnBlur(elem) {
                if(elem.name[elem.name.length - 1] === '-')
                    elem.name = elem.name.slice(0, elem.name.length - 1);
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